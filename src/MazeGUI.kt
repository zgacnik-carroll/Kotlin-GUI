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

        val titlePanel = createTitlePanel()
        val levelPanel = createLevelSelectPanel()

        container.add(titlePanel, "TITLE")
        container.add(levelPanel, "LEVELS")
        container.add(gamePanel, "GAME")

        add(container)
        pack()
        setLocationRelativeTo(null)

        cards.show(container, "TITLE")
    }

    private fun createTitlePanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        val title = JLabel("MAZE ESCAPE")
        val description = JLabel(
            """
                <html>
                <div style='margin-left:20px;'><b>Instructions:</b><br></div>
                <ul>
                <li>Move your player with WASD or the arrow keys</li>
                <li>Avoid walls (black boxes)</li>
                <li>Reach the exit (red block)</li>
                </ul>
                </html>""".trimIndent()
        )

        title.font = Font("Segoe UI", Font.BOLD, 48)
        title.alignmentX = Component.CENTER_ALIGNMENT
        description.alignmentX = Component.CENTER_ALIGNMENT
        description.font = Font("Segoe UI", Font.PLAIN, 20)

        val startButton = JButton("Start")
        startButton.alignmentX = Component.CENTER_ALIGNMENT

        startButton.addActionListener {
            cards.show(container, "LEVELS")
            pack()
        }

        panel.add(Box.createVerticalGlue())
        panel.add(title)
        panel.add(description)
        panel.add(Box.createRigidArea(Dimension(0, 30)))
        panel.add(startButton)
        panel.add(Box.createVerticalGlue())

        return panel
    }

    private fun createLevelSelectPanel(): JPanel {
        val panel = JPanel(GridLayout(5, 1, 10, 10))
        panel.border = BorderFactory.createEmptyBorder(40, 40, 40, 40)

        val levels = listOf(level1(), level2(), level3(), level4(), level5())

        for (i in levels.indices) {
            val btn = JButton("Level ${i + 1}")
            btn.addActionListener {
                gamePanel.loadLevel(levels[i])
                cards.show(container, "GAME")
                pack()
                gamePanel.requestFocusInWindow()
            }
            panel.add(btn)
        }

        return panel
    }

    inner class MazePanel : JPanel() {

        private val tileSize = 30
        private lateinit var maze: Array<CharArray>

        private var playerRow = 1
        private var playerCol = 1

        init {
            isFocusable = true
            setupKeyBindings()
        }

        fun loadLevel(level: Array<CharArray>) {
            maze = level.map { it.copyOf() }.toTypedArray()
            playerRow = 1
            playerCol = 1
            revalidate()
            repaint()
        }

        private fun setupKeyBindings() {
            val inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW)
            val actionMap = actionMap

            fun bind(key: String, name: String, action: () -> Unit) {
                inputMap.put(KeyStroke.getKeyStroke(key), name)
                actionMap.put(name, object : AbstractAction() {
                    override fun actionPerformed(e: java.awt.event.ActionEvent?) {
                        move(action)
                    }
                })
            }

            bind("W", "up") { move(-1, 0) }
            bind("UP", "up2") { move(-1, 0) }
            bind("S", "down") { move(1, 0) }
            bind("DOWN", "down2") { move(1, 0) }
            bind("A", "left") { move(0, -1) }
            bind("LEFT", "left2") { move(0, -1) }
            bind("D", "right") { move(0, 1) }
            bind("RIGHT", "right2") { move(0, 1) }
        }

        override fun move(dr: Int, dc: Int) {
            val nr = playerRow + dr
            val nc = playerCol + dc

            when (maze[nr][nc]) {
                '#' -> Toolkit.getDefaultToolkit().beep()
                'E' -> winDialog()
                else -> {
                    playerRow = nr
                    playerCol = nc
                    repaint()
                }
            }
        }

        private fun move(action: () -> Unit) = action()

        private fun winDialog() {
            val option = JOptionPane.showOptionDialog(
                this,
                "Level complete!",
                "Victory",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                arrayOf("Level Select", "Exit"),
                "Level Select"
            )

            if (option == 0) {
                cards.show(container, "LEVELS")
                pack()
            } else exitProcess(0)
        }

        override fun getPreferredSize(): Dimension {
            if (!::maze.isInitialized) return Dimension(600, 400)
            return Dimension(maze[0].size * tileSize, maze.size * tileSize)
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            if (!::maze.isInitialized) return

            for (r in maze.indices)
                for (c in maze[r].indices) {
                    when (maze[r][c]) {
                        '#' -> g.color = Color.BLACK
                        'E' -> g.color = Color.RED
                        else -> g.color = Color.WHITE
                    }
                    g.fillRect(c * tileSize, r * tileSize, tileSize, tileSize)
                    g.color = Color.GRAY
                    g.drawRect(c * tileSize, r * tileSize, tileSize, tileSize)
                }

            g.color = Color.GREEN
            g.fillOval(playerCol * tileSize, playerRow * tileSize, tileSize, tileSize)
        }
    }

    private fun level1() = arrayOf(
        "####################".toCharArray(),
        "#    #       #     #".toCharArray(),
        "# ## # ##### # ### #".toCharArray(),
        "#    #     # #   # #".toCharArray(),
        "#### ##### # ### # #".toCharArray(),
        "#        # #     # #".toCharArray(),
        "# ###### # ##### # #".toCharArray(),
        "#      #         # #".toCharArray(),
        "# #### # #######   #".toCharArray(),
        "##################E#".toCharArray()
    )

    private fun level2() = arrayOf(
        "####################".toCharArray(),
        "#  ##      #       #".toCharArray(),
        "# ## # ### # ### ###".toCharArray(),
        "#    #   #   #     #".toCharArray(),
        "#### ### ##### ### #".toCharArray(),
        "#     #     #      #".toCharArray(),
        "# ### ##### ###  ###".toCharArray(),
        "#   #       #      #".toCharArray(),
        "# ### ####### #### #".toCharArray(),
        "##################E#".toCharArray()
    )

    private fun level3() = arrayOf(
        "####################".toCharArray(),
        "#  #     #   #    ##".toCharArray(),
        "# ## ### # # ### ###".toCharArray(),
        "#    #   # #      ##".toCharArray(),
        "### ###### ###### ##".toCharArray(),
        "#     #       #   ##".toCharArray(),
        "# ### ####### ### ##".toCharArray(),
        "#   #         #   ##".toCharArray(),
        "# ### ####### ### ##".toCharArray(),
        "#################E##".toCharArray()
    )

    private fun level4() = arrayOf(
        "####################".toCharArray(),
        "#  #     #   #    ##".toCharArray(),
        "# ## ### # # ### ###".toCharArray(),
        "# #  #   # # #    ##".toCharArray(),
        "# # ###### ###### ##".toCharArray(),
        "#   #          #  ##".toCharArray(),
        "# ### ####### ### ##".toCharArray(),
        "#        #         #".toCharArray(),
        "# ### ####### #### #".toCharArray(),
        "##################E#".toCharArray()
    )

    private fun level5() = arrayOf(
        "####################".toCharArray(),
        "#              #  ##".toCharArray(),
        "# ## ### # ### ### #".toCharArray(),
        "# ## #   # #   #  ##".toCharArray(),
        "### ###### # #### ##".toCharArray(),
        "#   #  #      #   ##".toCharArray(),
        "# ### ####### ### ##".toCharArray(),
        "#   #   #          #".toCharArray(),
        "# ### ####### #### #".toCharArray(),
        "##################E#".toCharArray()
    )
}

fun main() {
    SwingUtilities.invokeLater { MazeGUI().isVisible = true }
}