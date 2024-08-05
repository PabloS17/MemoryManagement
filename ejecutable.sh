#!/bin/bash

# Instalar Makefile (si no está instalado)
sudo dnf install -y make

# Instalar Java 17 (si no está instalado)
sudo dnf install -y java-17-openjdk

# Instalar Maven (si no está instalado)
sudo dnf install -y maven

# Comprobar la instalación de Java
java -version

# Comprobar la instalación de Maven
mvn -version

# Compilar y ejecutar el proyecto Java con Maven
mvn clean javafx:run


