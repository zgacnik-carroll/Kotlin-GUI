import java.awt.*
import javax.swing.*
import kotlin.system.exitProcess

class MazeGUI : JFrame() {

    private val cards = CardLayout()
    private val container = JPanel(cards)

    private var gamePanel: MazePanel

    init {
        title = "Maze Escape"
        defaultCloseOperation = EXIT_ON_CLOSE
        isResizable = false

        gamePanel = MazePanel()
        val titlePanel = createTitlePanel()

        container.background = Color.WHITE
        contentPane.background = Color.WHITE

        container.add(titlePanel, "TITLE")
        container.add(gamePanel, "GAME")

        add(container)
        pack()
        setLocationRelativeTo(null)

        cards.show(container, "TITLE")
    }

    private fun createTitlePanel(): JPanel {

        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createEmptyBorder(40, 40, 40, 40)
        panel.background = Color(245, 245, 245)

        val title = JLabel("MAZE ESCAPE")
        title.font = Font("Segoe UI", Font.BOLD, 48)
        title.alignmentX = Component.CENTER_ALIGNMENT

        val instructions = JLabel(
            """
            <html>
            <b>Instructions:</b><br><br>
            <ul>
            <li>Use W/A/S/D or arrow keys to move</li><br>
            <li>Avoid walls (black squares)</li><br>
            <li>Reach the exit (red square)</li>
            </ul>
            </html>
            """.trimIndent()
        )
        instructions.alignmentX = Component.CENTER_ALIGNMENT

        val startButton = JButton("Start Game")
        startButton.font = Font("Segoe UI", Font.BOLD, 18)
        startButton.alignmentX = Component.CENTER_ALIGNMENT

        startButton.addActionListener {
            cards.show(container, "GAME")
            SwingUtilities.invokeLater {
                pack()
                gamePanel.requestFocusInWindow()
            }
        }

        panel.add(Box.createVerticalGlue())
        panel.add(title)
        panel.add(Box.createRigidArea(Dimension(0, 25)))
        panel.add(instructions)
        panel.add(Box.createRigidArea(Dimension(0, 25)))
        panel.add(startButton)
        panel.add(Box.createVerticalGlue())

        return panel
    }

    inner class MazePanel : JPanel() {

        private val tileSize = 30
        private val maze = createMaze()

        private var playerRow = 1
        private var playerCol = 1

        init {
            isFocusable = true
            background = Color.WHITE
            layout = null
            setupKeyBindings()

            val size = getPreferredSize()
            minimumSize = size
            maximumSize = size
            preferredSize = size
        }

        override fun doLayout() {
            super.doLayout()
            setSize(preferredSize)
        }

        private fun setupKeyBindings() {
            val inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW)
            val actionMap = actionMap

            fun bind(key: String, actionName: String, action: () -> Unit) {
                inputMap.put(KeyStroke.getKeyStroke(key), actionName)
                actionMap.put(actionName, object : AbstractAction() {
                    override fun actionPerformed(e: java.awt.event.ActionEvent?) {
                        action()
                        repaint()
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

        private fun createMaze(): Array<CharArray> {
            return arrayOf(
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
        }

        private fun resetGame() {
            playerRow = 1
            playerCol = 1
        }

        override fun move(dr: Int, dc: Int) {
            val newRow = playerRow + dr
            val newCol = playerCol + dc

            val nextTile = maze[newRow][newCol]

            when (nextTile) {
                '#' -> Toolkit.getDefaultToolkit().beep()
                'E' -> showEndDialog()
                else -> {
                    playerRow = newRow
                    playerCol = newCol
                }
            }
        }

        private fun showEndDialog() {
            val option = JOptionPane.showOptionDialog(
                this,
                "You escaped the maze!",
                "Victory",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                arrayOf("Restart", "Exit"),
                "Restart"
            )

            if (option == 0) {
                resetGame()
                repaint()
            } else {
                exitProcess(0)
            }
        }

        override fun getPreferredSize(): Dimension {
            return Dimension(maze[0].size * tileSize, maze.size * tileSize)
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)

            for (r in maze.indices) {
                for (c in maze[r].indices) {

                    when (maze[r][c]) {
                        '#' -> {
                            g.color = Color.BLACK
                            g.fillRect(c * tileSize, r * tileSize, tileSize, tileSize)
                        }
                        'E' -> {
                            g.color = Color.RED
                            g.fillRect(c * tileSize, r * tileSize, tileSize, tileSize)
                        }
                        else -> {
                            g.color = Color.WHITE
                            g.fillRect(c * tileSize, r * tileSize, tileSize, tileSize)
                        }
                    }

                    g.color = Color.GRAY
                    g.drawRect(c * tileSize, r * tileSize, tileSize, tileSize)
                }
            }

            g.color = Color.GREEN
            g.fillOval(playerCol * tileSize, playerRow * tileSize, tileSize, tileSize)
        }
    }
}

fun main() {
    SwingUtilities.invokeLater {
        MazeGUI().isVisible = true
    }
}