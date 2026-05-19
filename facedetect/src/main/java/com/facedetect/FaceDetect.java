package com.facedetect;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.*;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_objdetect.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;

public class FaceDetect {

    // ── Estado global ─────────────────────────────────────────────
    private static final AtomicBoolean running   = new AtomicBoolean(false);
    private static final AtomicBoolean paused    = new AtomicBoolean(false);
    private static int    faceCount     = 0;
    private static int    totalDetected = 0;
    private static long   startTime     = 0;
    private static int    frameCount    = 0;
    private static double currentFPS    = 0;

    // ── Colores UI ────────────────────────────────────────────────
    static final Color BG_DARK   = new Color(15, 17, 23);
    static final Color BG_PANEL  = new Color(22, 27, 34);
    static final Color BG_CARD   = new Color(30, 37, 46);
    static final Color ACCENT    = new Color(0, 210, 140);
    static final Color ACCENT2   = new Color(88, 166, 255);
    static final Color RED_ALERT = new Color(255, 80, 80);
    static final Color TEXT_PRI  = new Color(230, 237, 243);
    static final Color TEXT_SEC  = new Color(125, 133, 144);
    static final Color RECT_FACE = new Color(0, 210, 140);

    // ── Componentes UI ────────────────────────────────────────────
    static JLabel  videoLabel;
    static JLabel  lblFaces, lblTotal, lblFPS, lblStatus, lblTime;
    static JButton btnStart, btnPause, btnCapture;
    static JTextArea logArea;

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(FaceDetect::buildUI);
    }

    // ════════════════════════════════════════════════════════════════
    //  INTERFAZ GRÁFICA
    // ════════════════════════════════════════════════════════════════
    static void buildUI() {
        JFrame frame = new JFrame("FaceDetect — Detección de Rostros en Tiempo Real");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 720);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setBackground(BG_DARK);
        frame.getContentPane().setBackground(BG_DARK);

        // ── Panel principal ──────────────────────────────────────
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(BG_DARK);
        main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ── Header ───────────────────────────────────────────────
        main.add(buildHeader(), BorderLayout.NORTH);

        // ── Video ────────────────────────────────────────────────
        videoLabel = new JLabel("Sin señal de cámara", SwingConstants.CENTER);
        videoLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        videoLabel.setForeground(TEXT_SEC);
        videoLabel.setBackground(new Color(10, 12, 17));
        videoLabel.setOpaque(true);
        videoLabel.setBorder(BorderFactory.createLineBorder(new Color(48, 54, 61), 2));
        videoLabel.setPreferredSize(new Dimension(760, 520));

        JPanel videoPanel = new JPanel(new BorderLayout());
        videoPanel.setBackground(BG_DARK);
        videoPanel.add(videoLabel, BorderLayout.CENTER);

        // ── Panel lateral ────────────────────────────────────────
        JPanel sidebar = buildSidebar();
        sidebar.setPreferredSize(new Dimension(280, 0));

        JPanel center = new JPanel(new BorderLayout(10, 0));
        center.setBackground(BG_DARK);
        center.add(videoPanel, BorderLayout.CENTER);
        center.add(sidebar,    BorderLayout.EAST);
        main.add(center, BorderLayout.CENTER);

        // ── Footer / Log ─────────────────────────────────────────
        main.add(buildLog(), BorderLayout.SOUTH);

        frame.add(main);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        log("Sistema iniciado. Presiona ▶ Iniciar para activar la cámara.");
    }

    static JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(48, 54, 61), 1),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        JLabel title = new JLabel(" FaceDetect");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ACCENT);

        JLabel sub = new JLabel("Detección de Rostros en Tiempo Real · OpenCV + JavaCV");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(TEXT_SEC);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(BG_PANEL);
        left.add(title);
        left.add(sub);

        lblStatus = new JLabel("● DETENIDO");
        lblStatus.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblStatus.setForeground(TEXT_SEC);

        p.add(left,       BorderLayout.WEST);
        p.add(lblStatus,  BorderLayout.EAST);
        return p;
    }

    static JPanel buildSidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_DARK);

        // Métricas
        p.add(buildCard("Métricas en Vivo", new JPanel() {{
            setLayout(new GridLayout(4, 1, 0, 8));
            setBackground(BG_CARD);
            lblFaces = metricRow(this, "Rostros actuales", "0");
            lblTotal = metricRow(this, "Total detectados", "0");
            lblFPS   = metricRow(this, "FPS",              "0");
            lblTime  = metricRow(this, "Tiempo activo",    "00:00");
        }}));

        p.add(Box.createVerticalStrut(10));

        // Controles
        p.add(buildCard("🎮 Controles", new JPanel() {{
            setLayout(new GridLayout(3, 1, 0, 8));
            setBackground(BG_CARD);

            btnStart   = styledButton("▶  Iniciar cámara",  ACCENT,    Color.BLACK);
            btnPause   = styledButton("⏸  Pausar",          ACCENT2,   Color.BLACK);
            btnCapture = styledButton("📷 Capturar pantalla", BG_PANEL, TEXT_PRI);

            btnPause.setEnabled(false);
            btnCapture.setEnabled(false);

            add(btnStart);
            add(btnPause);
            add(btnCapture);

            btnStart.addActionListener(e -> toggleCamera());
            btnPause.addActionListener(e -> togglePause());
            btnCapture.addActionListener(e -> captureScreen());
        }}));

        p.add(Box.createVerticalStrut(10));

        // Info técnica
        p.add(buildCard("Configuración", new JPanel() {{
            setLayout(new GridLayout(3, 1, 0, 4));
            setBackground(BG_CARD);
            infoRow(this, "Clasificador", "Haar Cascade");
            infoRow(this, "Modelo",       "haarcascade_frontalface");
            infoRow(this, "Cámara",       "Índice 0 (por defecto)");
        }}));

        p.add(Box.createVerticalGlue());
        return p;
    }

    static JLabel metricRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG_CARD);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_SEC);
        JLabel val = new JLabel(value, SwingConstants.RIGHT);
        val.setFont(new Font("Monospaced", Font.BOLD, 14));
        val.setForeground(ACCENT);
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        parent.add(row);
        return val;
    }

    static void infoRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG_CARD);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_SEC);
        JLabel val = new JLabel(value, SwingConstants.RIGHT);
        val.setFont(new Font("Segoe UI", Font.BOLD, 11));
        val.setForeground(TEXT_PRI);
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        parent.add(row);
    }

    static JPanel buildCard(String title, JPanel content) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(48, 54, 61), 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_PRI);
        card.add(lbl,     BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height + 60));
        return card;
    }

    static JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() :
                            getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 36));
        return btn;
    }

    static JPanel buildLog() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(48, 54, 61), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        p.setPreferredSize(new Dimension(0, 90));

        JLabel title = new JLabel(" Log del sistema");
        title.setFont(new Font("Segoe UI", Font.BOLD, 11));
        title.setForeground(TEXT_SEC);

        logArea = new JTextArea(3, 0);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setForeground(ACCENT);
        logArea.setBackground(BG_PANEL);
        logArea.setEditable(false);
        logArea.setBorder(null);

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(null);
        scroll.setBackground(BG_PANEL);
        scroll.getViewport().setBackground(BG_PANEL);

        p.add(title, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ════════════════════════════════════════════════════════════════
    //  LÓGICA DE CÁMARA Y DETECCIÓN
    // ════════════════════════════════════════════════════════════════
    static void toggleCamera() {
        if (!running.get()) {
            running.set(true);
            paused.set(false);
            btnStart.setText("⏹  Detener cámara");
            btnStart.setBackground(RED_ALERT);
            btnPause.setEnabled(true);
            btnCapture.setEnabled(true);
            lblStatus.setText("● EN VIVO");
            lblStatus.setForeground(ACCENT);
            startTime = System.currentTimeMillis();
            log("Cámara iniciada. Cargando clasificador Haar Cascade...");
            new Thread(FaceDetect::runDetection).start();
            startTimer();
        } else {
            running.set(false);
            btnStart.setText("▶  Iniciar cámara");
            btnStart.setBackground(ACCENT);
            btnPause.setEnabled(false);
            btnCapture.setEnabled(false);
            lblStatus.setText("● DETENIDO");
            lblStatus.setForeground(TEXT_SEC);
            videoLabel.setIcon(null);
            videoLabel.setText("Sin señal de cámara");
            log("Cámara detenida.");
        }
    }

    static void togglePause() {
        if (paused.get()) {
            paused.set(false);
            btnPause.setText("⏸  Pausar");
            lblStatus.setText("● EN VIVO");
            lblStatus.setForeground(ACCENT);
            log("Detección reanudada.");
        } else {
            paused.set(true);
            btnPause.setText("▶  Reanudar");
            lblStatus.setText("● PAUSADO");
            lblStatus.setForeground(new Color(255, 200, 0));
            log("Detección pausada.");
        }
    }

    static void captureScreen() {
        if (videoLabel.getIcon() instanceof ImageIcon icon) {
            try {
                String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File out  = new File("captura_" + ts + ".png");
                ImageIO.write((BufferedImage)((ImageIcon) icon).getImage(), "png", out);
                log("📷 Captura guardada: " + out.getAbsolutePath());
                JOptionPane.showMessageDialog(null,
                    "Captura guardada:\n" + out.getAbsolutePath(),
                    "Captura exitosa", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                log("Error al guardar captura: " + e.getMessage());
            }
        }
    }

    static void runDetection() {
        OpenCVFrameGrabber grabber = null;
        try {
            // Cargar clasificador Haar Cascade
            CascadeClassifier faceClassifier = loadClassifier();
            if (faceClassifier == null) {
                log("Error: no se pudo cargar el clasificador de rostros.");
                return;
            }
            log("Clasificador cargado correctamente.");

            // Iniciar captura de cámara
            grabber = new OpenCVFrameGrabber(0);
            grabber.setImageWidth(640);
            grabber.setImageHeight(480);
            grabber.start();
            log("Cámara activa — resolución: " + grabber.getImageWidth() + "x" + grabber.getImageHeight());

            OpenCVFrameConverter.ToMat   toMat   = new OpenCVFrameConverter.ToMat();
            Java2DFrameConverter         toImage = new Java2DFrameConverter();
            long lastFPSTime = System.currentTimeMillis();
            int  fpsFrames   = 0;

            while (running.get()) {
                if (paused.get()) { Thread.sleep(50); continue; }

                Frame frame = grabber.grab();
                if (frame == null) continue;

                Mat mat = toMat.convert(frame);
                if (mat == null || mat.empty()) continue;

                // ── Detección de rostros ───────────────────────────
                Mat grayMat = new Mat();
                cvtColor(mat, grayMat, COLOR_BGR2GRAY);
                equalizeHist(grayMat, grayMat);

                RectVector faces = new RectVector();
                faceClassifier.detectMultiScale(
                    grayMat, faces,
                    1.1,       // scaleFactor
                    4,         // minNeighbors
                    0,         // flags
                    new Size(60, 60),   // minSize
                    new Size(0, 0)      // maxSize (sin límite)
                );

                int detected = (int) faces.size();
                if (detected != faceCount) {
                    faceCount = detected;
                    if (detected > 0) {
                        totalDetected += detected;
                        log("👤 " + detected + " rostro(s) detectado(s).");
                    }
                }

                // ── Dibujar rectángulos y etiquetas ───────────────
                for (int i = 0; i < faces.size(); i++) {
                    Rect r = faces.get(i);

                    // Rectángulo principal
                    rectangle(mat,
                        new Point(r.x(), r.y()),
                        new Point(r.x() + r.width(), r.y() + r.height()),
                        new Scalar(0, 210, 140, 255), 2, 8, 0);

                    // Esquinas decorativas
                    drawCorners(mat, r);

                    // Etiqueta
                    String label = "Rostro " + (i + 1);
                    int    baseLine[] = {0};
                    Size   textSize   = getTextSize(label, FONT_HERSHEY_SIMPLEX, 0.5, 1, baseLine);
                    rectangle(mat,
                        new Point(r.x(), r.y() - textSize.height() - 8),
                        new Point(r.x() + textSize.width() + 6, r.y()),
                        new Scalar(0, 210, 140, 255), -1, 8, 0);
                    putText(mat, label,
                        new Point(r.x() + 3, r.y() - 4),
                        FONT_HERSHEY_SIMPLEX, 0.5,
                        new Scalar(0, 0, 0, 255), 1, LINE_AA, false);
                }

                // ── Overlay de información ─────────────────────────
                String info = String.format("Rostros: %d  |  FPS: %.1f", detected, currentFPS);
                putText(mat, info, new Point(10, 25),
                    FONT_HERSHEY_SIMPLEX, 0.6,
                    new Scalar(0, 0, 0, 255), 3, LINE_AA, false);
                putText(mat, info, new Point(10, 25),
                    FONT_HERSHEY_SIMPLEX, 0.6,
                    new Scalar(0, 210, 140, 255), 1, LINE_AA, false);

                // ── Convertir a imagen Swing ───────────────────────
                Frame outFrame = toMat.convert(mat);
                BufferedImage img = toImage.convert(outFrame);
                if (img != null) {
                    int w = videoLabel.getWidth();
                    int h = videoLabel.getHeight();
                    if (w > 0 && h > 0) {
                        Image scaled = img.getScaledInstance(w, h, Image.SCALE_FAST);
                        SwingUtilities.invokeLater(() -> {
                            videoLabel.setIcon(new ImageIcon(scaled));
                            videoLabel.setText("");
                            lblFaces.setText(String.valueOf(faceCount));
                            lblTotal.setText(String.valueOf(totalDetected));
                            lblFPS.setText(String.format("%.1f", currentFPS));
                        });
                    }
                }

                // ── Calcular FPS ───────────────────────────────────
                fpsFrames++;
                frameCount++;
                long now = System.currentTimeMillis();
                if (now - lastFPSTime >= 1000) {
                    currentFPS  = fpsFrames * 1000.0 / (now - lastFPSTime);
                    fpsFrames   = 0;
                    lastFPSTime = now;
                }

                grayMat.close();
            }
        } catch (Exception e) {
            log("Error de cámara: " + e.getMessage());
            SwingUtilities.invokeLater(() -> {
                videoLabel.setIcon(null);
                videoLabel.setText("⚠ Error de cámara: " + e.getMessage());
                lblStatus.setText("● ERROR");
                lblStatus.setForeground(RED_ALERT);
                btnStart.setText("▶  Iniciar cámara");
                btnStart.setBackground(ACCENT);
                btnPause.setEnabled(false);
                btnCapture.setEnabled(false);
            });
        } finally {
            if (grabber != null) {
                try { grabber.stop(); } catch (Exception ignored) {}
            }
            running.set(false);
        }
    }

    // ── Esquinas decorativas sobre cada cara detectada ─────────────
    static void drawCorners(Mat mat, Rect r) {
        int len   = Math.min(r.width(), r.height()) / 6;
        int thick = 3;
        Scalar color = new Scalar(88, 166, 255, 255);
        int x = r.x(), y = r.y(), w = r.width(), h = r.height();

        // Superior izquierda
        rectangle(mat, new Point(x, y),         new Point(x + len, y + thick), color, -1, 8, 0);
        rectangle(mat, new Point(x, y),         new Point(x + thick, y + len), color, -1, 8, 0);
        // Superior derecha
        rectangle(mat, new Point(x+w-len, y),   new Point(x+w, y + thick),     color, -1, 8, 0);
        rectangle(mat, new Point(x+w-thick, y), new Point(x+w, y + len),       color, -1, 8, 0);
        // Inferior izquierda
        rectangle(mat, new Point(x, y+h-thick), new Point(x + len, y+h),       color, -1, 8, 0);
        rectangle(mat, new Point(x, y+h-len),   new Point(x+thick, y+h),       color, -1, 8, 0);
        // Inferior derecha
        rectangle(mat, new Point(x+w-len, y+h-thick), new Point(x+w, y+h),     color, -1, 8, 0);
        rectangle(mat, new Point(x+w-thick, y+h-len), new Point(x+w, y+h),     color, -1, 8, 0);
    }

    // ── Cargar clasificador Haar Cascade ──────────────────────────
    static CascadeClassifier loadClassifier() {
        try {
            // Extraer el XML del JAR a un archivo temporal
            InputStream is = FaceDetect.class
                .getResourceAsStream("/haarcascade_frontalface_default.xml");
            if (is == null) {
                log("⚠ Clasificador no encontrado en recursos. Descargando...");
                return downloadAndLoad();
            }
            File tmp = File.createTempFile("haarcascade", ".xml");
            tmp.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(tmp)) {
                is.transferTo(out);
            }
            CascadeClassifier cc = new CascadeClassifier(tmp.getAbsolutePath());
            return cc.empty() ? null : cc;
        } catch (Exception e) {
            log("Error cargando clasificador: " + e.getMessage());
            return downloadAndLoad();
        }
    }

    static CascadeClassifier downloadAndLoad() {
        try {
            // Buscar en la instalación de OpenCV local (JavaCV lo incluye)
            File tmp = File.createTempFile("haarcascade", ".xml");
            tmp.deleteOnExit();
            java.net.URL url = new java.net.URL(
                "https://raw.githubusercontent.com/opencv/opencv/master/" +
                "data/haarcascades/haarcascade_frontalface_default.xml");
            try (InputStream in = url.openStream();
                 FileOutputStream out = new FileOutputStream(tmp)) {
                in.transferTo(out);
            }
            log("Clasificador descargado correctamente.");
            CascadeClassifier cc = new CascadeClassifier(tmp.getAbsolutePath());
            return cc.empty() ? null : cc;
        } catch (Exception e) {
            log("❌ No se pudo obtener el clasificador: " + e.getMessage());
            return null;
        }
    }

    // ── Temporizador de tiempo activo ─────────────────────────────
    static void startTimer() {
        new Thread(() -> {
            while (running.get()) {
                long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                String time  = String.format("%02d:%02d", elapsed / 60, elapsed % 60);
                SwingUtilities.invokeLater(() -> lblTime.setText(time));
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    static void log(String msg) {
        String ts = new SimpleDateFormat("HH:mm:ss").format(new Date());
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + ts + "] " + msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}
