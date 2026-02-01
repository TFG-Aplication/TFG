package com.asistente.planificador.ui

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/*
 * PUNTO DE ENTRADA A NIVEL DE PROCESO (Application):
 * La anotación @HiltAndroidApp activa Hilt y crea el "cerebro" de la aplicación
 *
 * 1. Inicializa el grafo de dependencias antes de nada
 * 2. Mantiene vivas las instancias globales (Base de Datos, Firebase) mientras la app esté abierta
 * 3. Es el contenedor donde viven los objetos que no mueren al girar la pantalla.
 */

@HiltAndroidApp
class MyApp : Application() {
    // El cuerpo permanece vacío ya que Hilt automatiza la instanciación de los repositorios
    // y servicios definidos en los módulos de inyección (DataModule).
}