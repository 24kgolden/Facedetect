#!/bin/bash
# ══════════════════════════════════════════════════════════
#  FaceDetect — Script de construcción y ejecución
# ══════════════════════════════════════════════════════════

BOLD="\033[1m"; GREEN="\033[32m"; CYAN="\033[36m"
YELLOW="\033[33m"; RED="\033[31m"; RESET="\033[0m"

echo -e "${BOLD}${CYAN}"
echo "╔══════════════════════════════════════════════╗"
echo "║   FaceDetect — Detección de Rostros en Java  ║"
echo "╚══════════════════════════════════════════════╝"
echo -e "${RESET}"

# 1. Verificar Java
if ! command -v java &>/dev/null; then
    echo -e "${RED}✘ Java no encontrado.${RESET}"
    echo -e "  Instala con: ${YELLOW}sudo apt install openjdk-21-jdk${RESET}"
    exit 1
fi
echo -e "${GREEN}✔ Java: $(java -version 2>&1 | head -1)${RESET}"

# 2. Verificar Maven
if ! command -v mvn &>/dev/null; then
    echo -e "${RED}✘ Maven no encontrado.${RESET}"
    echo -e "  Instala con: ${YELLOW}sudo apt install maven${RESET}"
    exit 1
fi
echo -e "${GREEN}✔ Maven: $(mvn -version 2>&1 | head -1)${RESET}"

# 3. Compilar y empaquetar
echo ""
echo -e "${YELLOW}⚙  Compilando... (primera vez descarga dependencias ~200MB)${RESET}"
mvn clean package -q

if [ $? -ne 0 ]; then
    echo -e "${RED}✘ Error al compilar. Revisa los mensajes de Maven.${RESET}"
    exit 1
fi

echo -e "${GREEN}✔ Proyecto compilado: target/facedetect-1.0.jar${RESET}"
echo ""

# 4. Ejecutar
echo -e "${BOLD}Iniciando FaceDetect...${RESET}"
java -jar target/facedetect-1.0.jar
