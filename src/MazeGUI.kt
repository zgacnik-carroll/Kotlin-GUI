import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*

class MazeGUI : JFrame() {

    private val maze = arrayOf(
        "####################".toCharArray(),
        "#P   #       #     #".toCharArray(),
        "# ## # ##### # ### #".toCharArray(),
        "#    #     # #   # #".toCharArray(),
        "#### ##### # ### # #".toCharArray(),
        "#        # #     # #".toCharArray(),
        "# ###### # ##### # #".toCharArray(),
        "#      #         # #".toCharArray(),
        "# #### # #######   #".toCharArray(),
        "##################E#".toCharArray()
    )

    private var playerRow = 1
    private var playerCol = 1

    private val tileSize = 30

    init {
        title = "Maze Escape"
        defaultCloseOperation = EXIT_ON_CLOSE
        isResizable = false

        val panel = MazePanel()
        add(panel)

        pack()
        setLocationRelativeTo(null)

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                handleMove(e.keyCode)
                panel.repaint()
            }
        })
    }

    private fun handleMove(key: Int) {
        val (newRow, newCol) = when (key) {
            KeyEvent.VK_W, KeyEvent.VK_UP -> Pair(playerRow - 1, playerCol)
            KeyEvent.VK_S, KeyEvent.VK_DOWN -> Pair(playerRow + 1, playerCol)
            KeyEvent.VK_A, KeyEvent.VK_LEFT -> Pair(playerRow, playerCol - 1)
            KeyEvent.VK_D, KeyEvent.VK_RIGHT -> Pair(playerRow, playerCol + 1)
            else -> Pair(playerRow, playerCol)
        }

        val nextTile = maze[newRow][newCol]

        when (nextTile) {
            '#' -> Toolkit.getDefaultToolkit().beep()

            'E' -> {
                JOptionPane.showMessageDialog(this, "You escaped the maze!")
                System.exit(0)
            }

            ' ' -> {
                maze[playerRow][playerCol] = ' '
                playerRow = newRow
                playerCol = newCol
                maze[playerRow][playerCol] = 'P'
            }
        }
    }

    inner class MazePanel : JPanel() {

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

                        'P' -> {
                            g.color = Color.GREEN
                            g.fillOval(c * tileSize, r * tileSize, tileSize, tileSize)
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
        }
    }
}

fun main() {
    SwingUtilities.invokeLater {
        MazeGUI().isVisible = true
    }
}