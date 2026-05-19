<div align="center">

<img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
<img src="https://img.shields.io/badge/OpenCV-JavaCV-5C3EE8?style=for-the-badge&logo=opencv&logoColor=white"/>
<img src="https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white"/>
<img src="https://img.shields.io/badge/Linux-Compatible-FCC624?style=for-the-badge&logo=linux&logoColor=black"/>
<img src="https://img.shields.io/badge/License-MIT-22c55e?style=for-the-badge"/>

<br/><br/>

```
  ███████╗ █████╗  ██████╗███████╗██████╗ ███████╗████████╗███████╗ ██████╗████████╗
  ██╔════╝██╔══██╗██╔════╝██╔════╝██╔══██╗██╔════╝╚══██╔══╝██╔════╝██╔════╝╚══██╔══╝
  █████╗  ███████║██║     █████╗  ██║  ██║█████╗     ██║   █████╗  ██║        ██║
  ██╔══╝  ██╔══██║██║     ██╔══╝  ██║  ██║██╔══╝     ██║   ██╔══╝  ██║        ██║
  ██║     ██║  ██║╚██████╗███████╗██████╔╝███████╗   ██║   ███████╗╚██████╗   ██║
  ╚═╝     ╚═╝  ╚═╝ ╚═════╝╚══════╝╚═════╝ ╚══════╝   ╚═╝   ╚══════╝ ╚═════╝   ╚═╝
```

### 🎯 Detección de rostros en tiempo real con tu cámara web — Java + OpenCV

</div>

---

## 🖥️ Vista previa

```
┌─────────────────────────────────────────────┐
│  🎯 FaceDetect                  ● EN VIVO   │
│  Detección de Rostros en Tiempo Real        │
├──────────────────────────┬──────────────────┤
│                          │  📊 Métricas     │
│   ┌──────────────┐       │  Rostros: 2      │
│   │  Rostro 1    │       │  Total:   47     │
│   │              │       │  FPS:     28.4   │
│   └──────────────┘       │  Tiempo:  01:23  │
│                          ├──────────────────┤
│   ┌──────────────┐       │  🎮 Controles    │
│   │  Rostro 2    │       │  [⏹ Detener]     │
│   └──────────────┘       │  [⏸ Pausar ]     │
│                          │  [📷 Capturar]   │
│  Rostros: 2 | FPS: 28.4  ├──────────────────┤
│                          │  ⚙️ Config        │
└──────────────────────────┴──────────────────┘
```

---

## ✨ Características

- 📷 **Captura en vivo** desde cualquier cámara web conectada al PC
- 🟩 **Detección de múltiples rostros** simultáneos con rectángulos y etiquetas
- 🔵 **Esquinas decorativas** sobre cada rostro detectado (estilo HUD)
- 📊 **Métricas en tiempo real**: FPS, cantidad de rostros, tiempo activo y total detectados
- ⏸ **Pausa y reanudación** de la detección sin cerrar la app
- 📷 **Captura de pantalla** con un clic — guarda el frame actual como PNG
- 📋 **Log del sistema** con timestamp de cada evento
- 🎨 **Interfaz oscura** con diseño moderno tipo dashboard

---

## 🚀 Instalación y ejecución

### Requisitos previos

```bash
# Java 11 o superior
sudo apt install openjdk-21-jdk     # Ubuntu/Debian
sudo dnf install java-21-openjdk   # Fedora/RHEL

# Maven
sudo apt install maven

# Verificar
java -version && mvn -version
```

### Clonar y ejecutar

```bash
git clone https://github.com/tu-usuario/facedetect.git
cd facedetect
chmod +x run.sh
./run.sh
```

El script compila, empaqueta y ejecuta automáticamente.  
> ⚠️ La primera compilación descarga ~200 MB de dependencias (JavaCV + OpenCV).

### Ejecutar manualmente

```bash
mvn clean package -q
java -jar target/facedetect-1.0.jar
```

---

## ⚙️ Cómo funciona

```
Cámara Web
    │
    ▼
OpenCVFrameGrabber        ← Captura frames en tiempo real (JavaCV)
    │
    ▼
Conversión a Mat          ← Formato de imagen de OpenCV
    │
    ▼
cvtColor → GRAY           ← Convierte a escala de grises
equalizeHist              ← Ecualiza el histograma para mejor detección
    │
    ▼
CascadeClassifier         ← Aplica Haar Cascade (haarcascade_frontalface_default.xml)
detectMultiScale()        ← Detecta rostros a múltiples escalas
    │
    ▼
Dibujar rectángulos       ← Marca cada rostro con recuadro + esquinas + etiqueta
    │
    ▼
Java2DFrameConverter      ← Convierte Mat → BufferedImage
    │
    ▼
JLabel (Swing)            ← Muestra el frame en la ventana
```

---

## 🛠️ Tecnologías usadas

| Tecnología | Versión | Uso |
|-----------|---------|-----|
| Java | 21 | Lenguaje principal |
| [JavaCV](https://github.com/bytedeco/javacv) | 1.5.10 | Wrapper de OpenCV para Java |
| OpenCV | 4.9.0 (via JavaCV) | Procesamiento de imagen y detección |
| Haar Cascade | frontalface_default | Clasificador de rostros |
| Java Swing | JDK | Interfaz gráfica |
| Maven | 3.8+ | Gestión de dependencias y build |

---

## 📁 Estructura del proyecto

```
facedetect/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/facedetect/
│       │       └── FaceDetect.java    ← Clase principal
│       └── resources/
│           └── haarcascade_frontalface_default.xml  ← Clasificador (opcional)
├── target/                            ← Generado por Maven
│   └── facedetect-1.0.jar             ← JAR ejecutable con todas las dependencias
├── pom.xml                            ← Configuración Maven
├── run.sh                             ← Script de compilación y ejecución
└── README.md
```

---

## 🎛️ Parámetros de detección

Puedes ajustar la sensibilidad editando en `FaceDetect.java`:

```java
faceClassifier.detectMultiScale(
    grayMat,
    faces,
    1.1,              // scaleFactor  → menor = más detecciones, más lento
    4,                // minNeighbors → mayor = menos falsos positivos
    0,
    new Size(60, 60), // minSize      → tamaño mínimo de rostro detectado
    new Size(0, 0)    // maxSize      → 0 = sin límite
);
```

| Parámetro | Valor por defecto | Efecto |
|-----------|-----------------|--------|
| `scaleFactor` | `1.1` | Menor → más preciso pero más lento |
| `minNeighbors` | `4` | Mayor → menos falsos positivos |
| `minSize` | `60x60 px` | Ignorar rostros más pequeños que esto |

---

## 📋 Requisitos del sistema

- **SO**: Linux, macOS o Windows (con WSL)
- **Java**: 11 o superior (JDK)
- **RAM**: Mínimo 512 MB libre
- **Cámara**: Cualquier webcam USB o integrada (índice 0 por defecto)
- **Internet**: Requerido en la primera compilación para descargar JavaCV

---

## 🤝 Contribuciones

¡Bienvenidas! Para contribuir:

1. Haz un **fork** del repositorio
2. Crea tu rama: `git checkout -b feature/mi-mejora`
3. Haz commit: `git commit -m "feat: descripción"`
4. Push: `git push origin feature/mi-mejora`
5. Abre un **Pull Request**

### Ideas para contribuir

- [ ] Detección de ojos, boca y puntos faciales (landmarks)
- [ ] Reconocimiento facial con base de datos de rostros conocidos
- [ ] Grabación de video con detecciones en archivo MP4
- [ ] Notificación sonora cuando se detecta un nuevo rostro
- [ ] Soporte para múltiples cámaras simultáneas
- [ ] Exportar estadísticas de sesión a CSV
- [ ] Modo CLI sin interfaz gráfica

---

## 📄 Licencia

Este proyecto está bajo la licencia **MIT**. Consulta el archivo [LICENSE](LICENSE) para más detalles.

---

<div align="center">

Hecho con ☕ y Java · por [24kgolden](https://github.com/24kgolden)

<br/>

⭐ Si te fue útil, ¡dale una estrella al repositorio!

</div>
