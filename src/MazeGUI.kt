import java.awt.*
import javax.swing.*
import kotlin.system.exitProcess

/**
 * Main application window for the Maze Escape game.
 *
 * Uses a CardLayout to manage navigation between:
 * - Title screen
 * - Level selection screen
 * - Gameplay screen
 */
@Suppress("DEPRECATION")
class MazeGUI : JFrame() {

    /** Layout manager controlling screen transitions */
    private val cards = CardLayout()

    /** Root container holding all application screens */
    private val container = JPanel(cards)

    /** Gameplay panel responsible for rendering and logic */
    private val gamePanel = MazePanel()

    /**
     * Initializes the main frame and registers UI screens.
     */
    init {
        title = "Maze Escape"
        defaultCloseOperation = EXIT_ON_CLOSE
        isResizable = false

        // Register screens
        container.add(createTitlePanel(), "TITLE")
        container.add(createLevelPanel(), "LEVELS")
        container.add(gamePanel, "GAME")

        add(container)
        pack()
        setLocationRelativeTo(null)

        // Show title screen first
        cards.show(container, "TITLE")
    }

    /**
     * Creates a standardized styled button used throughout the UI.
     *
     * @param t Button label text
     * @return Styled JButton instance
     */
    private fun btn(t: String) = JButton(t).apply {
        font = Font("Segoe UI", Font.BOLD, 18)
        preferredSize = Dimension(220, 45)
        alignmentX = Component.CENTER_ALIGNMENT
    }

    /**
     * Builds the title screen panel with instructions and start button.
     *
     * @return Title screen panel
     */
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

        // Vertical layout composition
        p.add(Box.createVerticalGlue())
        p.add(title)
        p.add(description)
        p.add(Box.createRigidArea(Dimension(0, 30)))
        p.add(start)
        p.add(Box.createVerticalGlue())
        return p
    }

    /**
     * Builds the level selection screen containing available maze levels.
     *
     * @return Level selection panel
     */
    private fun createLevelPanel(): JPanel {
        val p = JPanel()
        p.layout = BoxLayout(p, BoxLayout.Y_AXIS)
        p.add(Box.createVerticalGlue())

        // Pre-generate fixed maze sizes for difficulty progression
        val levels = listOf(
            generateMaze(21, 11),
            generateMaze(25, 13),
            generateMaze(29, 15)
        )

        // Create buttons dynamically for each level
        levels.forEachIndexed { i, lvl ->
            val b = btn("Level ${i + 1}")
            b.addActionListener {
                gamePanel.loadLevel(lvl, i + 1)
                cards.show(container, "GAME")
                pack() // Resize window to maze size
            }
            p.add(b)
            p.add(Box.createRigidArea(Dimension(0, 10)))
        }

        // Larger maze for final challenge
        val endless = btn("Level 4")
        endless.addActionListener {
            gamePanel.loadLevel(generateMaze(35, 19), 99)
            cards.show(container, "GAME")
            pack()
        }

        val exit = btn("Exit Game")
        exit.addActionListener { exitProcess(0) }

        p.add(Box.createRigidArea(Dimension(0, 10)))
        p.add(endless)
        p.add(Box.createRigidArea(Dimension(0, 20)))
        p.add(exit)
        p.add(Box.createVerticalGlue())

        return p
    }

    /**
     * Panel responsible for gameplay rendering, animation,
     * input handling, and HUD display.
     */
    inner class MazePanel : JPanel() {

        /** Pixel size of each maze tile */
        private val tile = 28

        /** Maze grid representation */
        private lateinit var maze: Array<CharArray>

        /** Player grid coordinates */
        private var playerR = 1
        private var playerC = 1

        /** Interpolated player position used for animation */
        private var px = 1.0
        private var py = 1.0

        /** Indicates whether player movement animation is active */
        private var animating = false

        /** Level start timestamp for timer tracking */
        private var startTime = 0L

        /** HUD labels */
        private val timerLabel = JLabel()
        private val levelLabel = JLabel()

        /** Victory overlay */
        private val victory = JPanel()

        /** HUD buttons (stored so we can enable/disable them) */
        private val restartBtn = JButton("Restart")
        private val quitBtn = JButton("Quit")

        init {
            isFocusable = true
            layout = OverlayLayout(this)
            createHUD()
            createVictory()
            setupKeys()
        }

        /**
         * Creates the heads-up display containing timer, level info,
         * and control buttons.
         */
        private fun createHUD() {
            val hud = JPanel(FlowLayout(FlowLayout.LEFT))
            hud.isOpaque = false

            restartBtn.addActionListener { loadLevel(maze, 0) }
            quitBtn.addActionListener { cards.show(container, "LEVELS") }

            hud.add(levelLabel)
            hud.add(timerLabel)
            hud.add(restartBtn)
            hud.add(quitBtn)
            add(hud)
        }

        /**
         * Builds victory overlay displayed when player reaches exit.
         */
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
                restartBtn.isEnabled = true
                quitBtn.isEnabled = true
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

        /**
         * Loads a new maze level and resets player state.
         */
        fun loadLevel(level: Array<CharArray>, num: Int) {
            maze = level.map { it.copyOf() }.toTypedArray()

            // Reset player state
            playerR = 1
            playerC = 1
            px = playerC.toDouble()
            py = playerR.toDouble()
            animating = false
            victory.isVisible = false

            // Re-enable HUD buttons
            restartBtn.isEnabled = true
            quitBtn.isEnabled = true

            levelLabel.text = "Level $num"
            startTime = System.currentTimeMillis()
            repaint()
            requestFocusInWindow()
        }

        /**
         * Registers keyboard bindings for player movement.
         */
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

        /**
         * Attempts to move the player in a specified direction.
         */
        @Deprecated("Deprecated in Java")
        override fun move(dr: Int, dc: Int) {
            if (animating) return

            val nr = playerR + dr
            val nc = playerC + dc

            // Prevent out-of-bounds movement
            if (nr !in maze.indices || nc !in maze[0].indices) return

            when (maze[nr][nc]) {
                '#' -> Toolkit.getDefaultToolkit().beep()
                'E' -> finish()
                else -> animateMove(nr, nc)
            }
        }

        /**
         * Animates smooth player movement between tiles.
         */
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

                // Linear interpolation for smooth motion
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

        /**
         * Handles level completion logic and victory display.
         */
        private fun finish() {
            val time = (System.currentTimeMillis() - startTime) / 1000

            val stars = when {
                time < 30 -> "★★★"
                time < 60 -> "★★"
                else -> "★"
            }

            (victory.components[0] as JPanel).components[0].let {
                (it as JLabel).apply {
                    font = Font("Dialog", Font.BOLD, 18)
                    text = "Victory! Time: ${time}s $stars"
                }
            }

            // Disable HUD interaction during victory overlay
            restartBtn.isEnabled = false
            quitBtn.isEnabled = false

            victory.isVisible = true
        }

        override fun getPreferredSize(): Dimension {
            if (!::maze.isInitialized) return Dimension(600, 400)
            return Dimension(maze[0].size * tile, maze.size * tile)
        }

        /**
         * Renders maze tiles and animated player sprite.
         */
        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            if (!::maze.isInitialized) return

            val time = (System.currentTimeMillis() - startTime) / 1000
            timerLabel.text = "Time: ${time}s"

            val g2 = g as Graphics2D

            // Draw maze tiles
            for (r in maze.indices)
                for (c in maze[r].indices) {
                    g2.color = when (maze[r][c]) {
                        '#' -> Color(40, 40, 40)
                        'E' -> Color(200, 60, 60)
                        else -> Color(240, 240, 240)
                    }
                    g2.fillRect(c * tile, r * tile, tile, tile)
                }

            // Player shadow
            g2.color = Color(0, 0, 0, 50)
            g2.fillOval((px * tile).toInt() + 3, (py * tile).toInt() + 3, tile, tile)

            // Player sprite
            g2.color = Color(60, 200, 90)
            g2.fillOval((px * tile).toInt(), (py * tile).toInt(), tile, tile)
        }
    }

    /**
     * Generates a perfect maze using randomized depth-first search.
     */
    private fun generateMaze(w: Int, h: Int): Array<CharArray> {
        val maze = Array(h) { CharArray(w) { '#' } }

        val stack = mutableListOf(1 to 1)
        maze[1][1] = ' '

        val dirs = listOf(2 to 0, -2 to 0, 0 to 2, 0 to -2)

        while (stack.isNotEmpty()) {
            val (r, c) = stack.last()

            val neighbors = dirs.map { (dr, dc) -> r + dr to c + dc }
                .filter { (nr, nc) ->
                    nr in 1 until h - 1 &&
                            nc in 1 until w - 1 &&
                            maze[nr][nc] == '#'
                }

            if (neighbors.isEmpty()) {
                stack.removeLast()
            } else {
                val (nr, nc) = neighbors.random()

                // Carve passage between cells
                maze[(r + nr) / 2][(c + nc) / 2] = ' '
                maze[nr][nc] = ' '

                stack.add(nr to nc)
            }
        }

        // Place exit
        maze[h - 2][w - 2] = 'E'
        return maze
    }
}

/**
 * Application entry point.
 */
fun main() {
    SwingUtilities.invokeLater { MazeGUI().isVisible = true }
}