import java.awt.*
import javax.swing.*
import kotlin.system.exitProcess

class MazeGUI : JFrame() {

    private val cards = CardLayout()
    private val container = JPanel(cards)
    private val gamePanel = MazePanel()

    init {
        title = "Maze Escape"
        defaultCloseOperation = EXIT_ON_CLOSE
        isResizable = false

        container.add(createTitlePanel(), "TITLE")
        container.add(createLevelPanel(), "LEVELS")
        container.add(gamePanel, "GAME")

        add(container)
        pack()
        setLocationRelativeTo(null)
        cards.show(container, "TITLE")
    }

    private fun btn(t: String) = JButton(t).apply {
        font = Font("Segoe UI", Font.BOLD, 18)
        preferredSize = Dimension(220, 45)
        alignmentX = Component.CENTER_ALIGNMENT
    }

    private fun createTitlePanel(): JPanel {
        val p = JPanel()
        p.layout = BoxLayout(p, BoxLayout.Y_AXIS)

        val title = JLabel("MAZE ESCAPE").apply {
            font = Font("Segoe UI", Font.BOLD, 50)
            alignmentX = Component.CENTER_ALIGNMENT
        }

        val description = JLabel("""
            <html>
            <div style='margin-left:20px;'><b>Instructions:</b></div>
            <ul>
            <li>Move using WASD or arrow keys</li>
            <li>Follow the white path</li>
            <li>Reach the finish (red square)</li>
            </ul>
            </html>
            """.trimIndent())

        description.alignmentX = Component.CENTER_ALIGNMENT
        description.font = Font("Segoe UI", Font.PLAIN, 24)

        val start = btn("Start")
        start.addActionListener { cards.show(container, "LEVELS") }

        p.add(Box.createVerticalGlue())
        p.add(title)
        p.add(description)
        p.add(Box.createRigidArea(Dimension(0, 30)))
        p.add(start)
        p.add(Box.createVerticalGlue())
        return p
    }

    private fun createLevelPanel(): JPanel {
        val p = JPanel()
        p.layout = BoxLayout(p, BoxLayout.Y_AXIS)

        val levels = listOf(
            generateMaze(21, 11),
            generateMaze(25, 13),
            generateMaze(29, 15)
        )

        levels.forEachIndexed { i, lvl ->
            val b = btn("Level ${i + 1}")
            b.addActionListener {
                gamePanel.loadLevel(lvl, i + 1)
                cards.show(container, "GAME")
                pack()
            }
            p.add(b)
            p.add(Box.createRigidArea(Dimension(0, 10)))
        }

        val endless = btn("Procedural Maze")
        endless.addActionListener {
            gamePanel.loadLevel(generateMaze(35, 19), 99)
            cards.show(container, "GAME")
            pack()
        }

        p.add(Box.createRigidArea(Dimension(0, 10)))
        p.add(endless)
        p.add(Box.createVerticalGlue())
        return p
    }

    inner class MazePanel : JPanel() {

        private val tile = 28
        private lateinit var maze: Array<CharArray>

        private var playerR = 1
        private var playerC = 1
        private var px = 1.0
        private var py = 1.0
        private var animating = false

        private var startTime = 0L
        private val timerLabel = JLabel()
        private val levelLabel = JLabel()

        private val victory = JPanel()

        init {
            isFocusable = true
            layout = OverlayLayout(this)
            createHUD()
            createVictory()
            setupKeys()
        }

        private fun createHUD() {
            val hud = JPanel(FlowLayout(FlowLayout.LEFT))
            hud.isOpaque = false

            val restart = JButton("Restart")
            val quit = JButton("Quit")

            restart.addActionListener { loadLevel(maze, 0) }
            quit.addActionListener { cards.show(container, "LEVELS") }

            hud.add(levelLabel)
            hud.add(timerLabel)
            hud.add(restart)
            hud.add(quit)
            add(hud)
        }

        private fun createVictory() {
            victory.layout = GridBagLayout()
            victory.background = Color(0, 0, 0, 160)
            victory.isVisible = false

            val box = JPanel()
            box.layout = BoxLayout(box, BoxLayout.Y_AXIS)
            box.background = Color.WHITE
            box.border = BorderFactory.createEmptyBorder(20, 40, 20, 40)

            val label = JLabel()
            label.font = Font("Segoe UI", Font.BOLD, 24)
            label.alignmentX = Component.CENTER_ALIGNMENT

            val levels = JButton("Level Select")
            val exit = JButton("Exit")

            levels.addActionListener {
                victory.isVisible = false
                cards.show(container, "LEVELS")
            }

            exit.addActionListener { exitProcess(0) }

            box.add(label)
            box.add(Box.createRigidArea(Dimension(0, 10)))
            box.add(levels)
            box.add(Box.createRigidArea(Dimension(0, 8)))
            box.add(exit)

            victory.add(box)
            add(victory)
        }

        fun loadLevel(level: Array<CharArray>, num: Int) {
            maze = level.map { it.copyOf() }.toTypedArray()
            playerR = 1
            playerC = 1
            px = playerC.toDouble()
            py = playerR.toDouble()
            animating = false
            victory.isVisible = false
            levelLabel.text = "Level $num"
            startTime = System.currentTimeMillis()
            repaint()
            requestFocusInWindow()
        }

        private fun setupKeys() {
            val im = getInputMap(WHEN_IN_FOCUSED_WINDOW)
            val am = actionMap

            fun bind(k: String, f: () -> Unit) {
                im.put(KeyStroke.getKeyStroke(k), k)
                am.put(k, object : AbstractAction() {
                    override fun actionPerformed(e: java.awt.event.ActionEvent?) = f()
                })
            }

            bind("W") { move(-1, 0) }
            bind("UP") { move(-1, 0) }
            bind("S") { move(1, 0) }
            bind("DOWN") { move(1, 0) }
            bind("A") { move(0, -1) }
            bind("LEFT") { move(0, -1) }
            bind("D") { move(0, 1) }
            bind("RIGHT") { move(0, 1) }
        }

        override fun move(dr: Int, dc: Int) {
            if (animating) return

            val nr = playerR + dr
            val nc = playerC + dc
            if (nr !in maze.indices || nc !in maze[0].indices) return

            when (maze[nr][nc]) {
                '#' -> Toolkit.getDefaultToolkit().beep()
                'E' -> finish()
                else -> animateMove(nr, nc)
            }
        }

        private fun animateMove(nr: Int, nc: Int) {
            animating = true
            val sx = px
            val sy = py
            val ex = nc.toDouble()
            val ey = nr.toDouble()

            val steps = 10
            var step = 0

            val timer = Timer(16) {
                step++
                val t = step / steps.toDouble()
                px = sx + (ex - sx) * t
                py = sy + (ey - sy) * t
                repaint()

                if (step >= steps) {
                    (it.source as Timer).stop()
                    playerR = nr
                    playerC = nc
                    px = ex
                    py = ey
                    animating = false
                }
            }
            timer.start()
        }

        private fun finish() {
            val time = (System.currentTimeMillis() - startTime) / 1000
            val stars = when {
                time <= 30 -> "★★★"
                time <= 60 -> "★★"
                else -> "★"
            }

            (victory.components[0] as JPanel).components[0].let {
                (it as JLabel).apply {
                    font = Font("Dialog", Font.BOLD, 18)
                    text = "Victory! Time: ${time}s $stars"
                }
            }

            victory.isVisible = true
        }

        override fun getPreferredSize(): Dimension {
            if (!::maze.isInitialized) return Dimension(600, 400)
            return Dimension(maze[0].size * tile, maze.size * tile)
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            if (!::maze.isInitialized) return

            val time = (System.currentTimeMillis() - startTime) / 1000
            timerLabel.text = "Time: ${time}s"

            val g2 = g as Graphics2D

            for (r in maze.indices)
                for (c in maze[r].indices) {
                    g2.color = when (maze[r][c]) {
                        '#' -> Color(40, 40, 40)
                        'E' -> Color(200, 60, 60)
                        else -> Color(240, 240, 240)
                    }
                    g2.fillRect(c * tile, r * tile, tile, tile)
                }

            g2.color = Color(0, 0, 0, 50)
            g2.fillOval((px * tile).toInt() + 3, (py * tile).toInt() + 3, tile, tile)

            g2.color = Color(60, 200, 90)
            g2.fillOval((px * tile).toInt(), (py * tile).toInt(), tile, tile)
        }
    }

    private fun generateMaze(w: Int, h: Int): Array<CharArray> {
        val maze = Array(h) { CharArray(w) { '#' } }
        val stack = mutableListOf(1 to 1)
        maze[1][1] = ' '

        val dirs = listOf(2 to 0, -2 to 0, 0 to 2, 0 to -2)

        while (stack.isNotEmpty()) {
            val (r, c) = stack.last()
            val neighbors = dirs.map { (dr, dc) -> r + dr to c + dc }
                .filter { (nr, nc) -> nr in 1 until h - 1 && nc in 1 until w - 1 && maze[nr][nc] == '#' }

            if (neighbors.isEmpty()) stack.removeLast()
            else {
                val (nr, nc) = neighbors.random()
                maze[(r + nr) / 2][(c + nc) / 2] = ' '
                maze[nr][nc] = ' '
                stack.add(nr to nc)
            }
        }

        maze[h - 2][w - 2] = 'E'
        return maze
    }
}

fun main() {
    SwingUtilities.invokeLater { MazeGUI().isVisible = true }
}