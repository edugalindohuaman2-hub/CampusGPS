# Registro de Prompts y Mejoras con IA (PROMPTS.md)

En este documento se documenta el proceso de interacción y desarrollo asistido para la implementación de las 3 mejoras funcionales en la rama `mejora-ia` del proyecto **CampusGPS**, así como el análisis técnico del cumplimiento de la arquitectura MVVM y las buenas prácticas de seguridad en Android.

---

## Tabla de Registro de Prompts

| Prompt que escribí | Qué respondió Gemini | Qué cambié o descarté | Por qué |
| :--- | :--- | :--- | :--- |
| **Mejora 1: Filtrar por categoría**<br>`En la rama mejora-ia, agrega categoriaSeleccionada: StateFlow<String?> (null = Todas) y fun filtrarPorCategoria(categoria: String?) en MapaViewModel.kt. Expón lugaresFiltrados combinando lugares y categoriaSeleccionada. En PantallaMapa.kt agrega una fila de FilterChip ("Todas", "Estudio", "Comida", "Eventos", "Servicios") pasando lugaresFiltrados a MapaUbicacion y ListaLugares.` | Gemini generó la lógica usando `combine` en `MapaViewModel` para filtrar los lugares por categoría y la fila de `FilterChip` usando `LazyRow` en `PantallaMapa.kt`. | Se ajustó la comparación de cadenas para ignorar mayúsculas y minúsculas (`equals(cat, ignoreCase = true)`) y se manejó la opción "Todas" mapeando a `null` implícito. | Para evitar discrepancias en caso de diferencias de formato o casing en los datos del repositorio y asegurar que "Todas" resetee el filtro limpiamente. |
| **Mejora 2: Punto azul nativo**<br>`Implementa la mejora "Punto azul nativo" en MapaUbicacion.kt: recibe tienePermiso: Boolean verificado con ContextCompat.checkSelfPermission en PantallaMapa.kt. Configura properties = MapProperties(isMyLocationEnabled = tienePermiso) y uiSettings = MapUiSettings(myLocationButtonEnabled = tienePermiso) dentro de GoogleMap.` | Gemini proporcionó el código para calcular `tienePermiso` en `PantallaMapa.kt` pasando el flag booleano a `MapaUbicacion.kt` para activar `MapProperties` y `MapUiSettings`. | Se colocó `modifier: Modifier = Modifier` antes de `tienePermiso: Boolean` en la firma de `MapaUbicacion` respetando la convención de Compose. | Para cumplir estrictamente con las pautas de estilo de Jetpack Compose donde `modifier` es el primer parámetro opcional. |
| **Mejora 3: Radio de cercanía**<br>`Agrega radioMetros: StateFlow<Float> (200f) y cambiarRadio(metros) en MapaViewModel.kt. Agrega selector de chips (100m, 200m, 500m) en PantallaMapa.kt. En MapaUbicacion.kt dibuja un Circle de maps-compose azul semitransparente centrado en la posición. En ListaLugares.kt marca los lugares <= radioMetros.` | Gemini generó la propiedad de estado en el ViewModel, los `FilterChip` de selección de radio (100m, 200m, 500m), el dibujo del `Circle` en `GoogleMap` y el resalte visual en la lista de lugares. | Se agregó un indicador visual explícito `• En radio` en verde (`#4CAF50`) con un borde diferenciado en las tarjetas de `ListaLugares`. | Para mejorar la experiencia de usuario (UX) haciendo evidente qué lugares están dentro del perímetro seleccionado además del mapa. |

---

## Análisis Técnico de Arquitectura y Seguridad

### 1. Respeto Estricto de la Arquitectura MVVM
- **Separación de Responsabilidades:** La capa de UI (`PantallaMapa`, `MapaUbicacion`, `ListaLugares`, `MensajeError`) es puramente declarativa y reactiva. Observa los estados expuestos por el ViewModel (`estado`, `lugaresFiltrados`, `categoriaSeleccionada`, `radioMetros`, `masCercano`, `seleccionado`) mediante `collectAsState()` y delega todas las acciones o eventos mediante funciones (`vm.buscar()`, `vm.seleccionar(id)`, `vm.filtrarPorCategoria(cat)`, `vm.cambiarRadio(radio)`).
- **Cálculos y Transformaciones:** El procesamiento de datos (cálculo de distancias con `Location.distanceBetween`, ordenamiento con `sortedBy`, derivación de `masCercano` con `stateIn` y combinación de filtros con `combine`) se ejecuta exclusivamente en el `MapaViewModel`, evitando recalcular distancias durante la recomposición de los composables.

### 2. Sin Inyección de `Context` en el ViewModel
- El `MapaViewModel` no mantiene referencias directas al `Context` de Android ni a clases dependientes del sistema operativo. 
- La verificación de permisos en tiempo de ejecución (`ContextCompat.checkSelfPermission`) y la creación del `Intent` para los ajustes del sistema se ejecutan estrictamente dentro de la capa de vista (`PantallaMapa.kt` y `MensajeError.kt`).
- El acceso a `LocationServices` y `GoogleApiAvailability` se encapsula dentro del repositorio (`UbicacionRepositoryGoogle`), el cual recibe el `Context` mediante inyección en el Factory (`CampusViewModelFactory`).

### 3. Protección de la API Key de Google Maps
- La API Key de Google Maps no se encuentra codificada ("hardcoded") en ningún archivo de código fuente de Kotlin (`.kt`) ni en el archivo `AndroidManifest.xml`.
- Se almacena exclusivamente en `local.properties` (`MAPS_API_KEY=...`), un archivo que está excluido del control de versiones en `.gitignore`.
- Gradle lee `local.properties` en `app/build.gradle.kts` mediante `java.util.Properties()` y la inyecta de forma segura a través de `manifestPlaceholders["MAPS_API_KEY"]`, siendo referenciada en el manifest como `${MAPS_API_KEY}`.
