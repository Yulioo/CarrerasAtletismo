# Carreras Atletismo

Aplicación Android escrita en **Kotlin** con **Jetpack Compose** y **Material 3** para administrar maratones, corredores, categorías e inscripciones. Funciona **sin internet** con una base de datos local **SQLite (Room)**. Si hay conexión, descarga maratones reales de la API pública de [RunDida](https://rundida.com).

Proyecto de la carrera de Desarrollo de Software Multiplataforma, Universidad Tecnológica de Tula-Tepeji (grupo 4DSM-G2).

**Autor:** Julio Cesar Lugo Rodríguez

## Funciones

- **Carreras**: lista con buscador (nombre, ciudad o país), detalle, agregar, editar y eliminar.
- **Corredores**: agregar, editar y eliminar. La categoría se asigna sola según la edad.
- **Categorías** (catálogo): agregar, editar y eliminar. No se puede borrar una categoría que tiene corredores.
- **Inscripciones** (relación N:M): inscribir un corredor en una carrera con dorsal y tiempo objetivo, editar y quitar la inscripción.
- **Resultados**: capturar el tiempo final y calcular la posición automáticamente.
- **Sincronización** con la API de RunDida. Solo se hace en automático si la base está vacía, y nunca borra datos del usuario.
- **Datos iniciales**: la primera vez que se abre trae carreras, corredores y categorías de ejemplo.
- **Manejo de errores**: validación de campos (obligatorio, longitud, formato numérico, fecha, URL y tiempo), nombres duplicados, dorsal ocupado, sin internet, confirmación antes de eliminar y mensajes personalizados en `strings.xml`.
- **Rotación**: el estado vive en ViewModels y `rememberSaveable`, así que al girar la pantalla no se pierde nada. Todas las pantallas se desplazan.

## Arquitectura (capas recomendadas por Android)

```
app/src/main/java/com/julio/carrerasatletismo/
├── CarrerasApp.kt              @HiltAndroidApp (Application)
├── MainActivity.kt             @AndroidEntryPoint, punto de entrada de la UI
├── data/
│   ├── local/                  Room: @Entity, @DatabaseView, @Database (Singleton), migración, datos iniciales
│   │   └── dao/                CarreraDao, CorredorDao, CategoriaDao, InscripcionDao
│   ├── remote/                 Retrofit: RunDidaApi, DTOs, RetrofitClient (object)
│   ├── mapper/                 Funciones de extensión Entity ↔ Dominio
│   └── repository/             CarrerasRepositoryImpl
├── domain/
│   ├── model/                  Modelos de dominio y ErrorDominio (excepciones propias)
│   └── repository/             Interfaz CarrerasRepository
├── di/                         Módulos de Hilt (@Provides, @Binds)
└── ui/
    ├── navigation/             NavHost y rutas
    ├── viewmodel/              @HiltViewModel + UiState de cada pantalla
    ├── screens/                Pantallas Compose con @Preview
    └── theme/                  Colores, tipografía y tema Material 3
```

### Base de datos (Room, versión 2)

| Entidad | Tabla | CRUD | Relación |
|---|---|---|---|
| `CarreraEntity` | carrera | ✔ | N:M con corredor (vía inscripcion) |
| `CorredorEntity` | corredor | ✔ | N:1 con categoria |
| `CategoriaEntity` | categoria | ✔ | 1:N con corredor (`CategoriaConCorredores`) |
| `InscripcionEntity` | inscripcion | ✔ | Tabla intermedia N:M (`CorredorConCarreras` con `Junction`) |

Las tablas de apoyo son `pais`, `ciudad` y `resultado`, y las vistas son `carrera_vista` y `corredor_vista`.

## Patrones de diseño

| Patrón | Dónde |
|---|---|
| **Inyección de dependencias (Hilt)** | `CarrerasApp`, `MainActivity`, `di/ModuloDatos.kt`, `di/ModuloRepositorio.kt`, `@HiltViewModel` en `ui/viewmodel/` |
| **Singleton** | `AppDatabase.obtenerInstancia()` (`@Volatile` + `synchronized`), `RetrofitClient` (`object`), `@Singleton` en Hilt |
| **Repository** | `domain/repository/CarrerasRepository.kt` (interfaz) y `data/repository/CarrerasRepositoryImpl.kt` |
| **Observer** | DAO `Flow` → `StateFlow` (`stateIn`) en ViewModel → `collectAsState()` en pantalla |
| **UDF** | Cada pantalla recibe un `UiState` y lambdas `onXxxChange` / `onGuardar`; los eventos suben al ViewModel y el estado baja |

## Tecnologías

| Herramienta | Versión |
|---|---|
| Kotlin (integrado en AGP 9) | 2.3.20 |
| Android Gradle Plugin | 9.2.0 |
| Gradle | 9.4.1 |
| KSP | 2.3.7 |
| Compose BOM / Material 3 | 2026.08.00 |
| Navigation Compose | 2.9.8 |
| Lifecycle (ViewModel) | 2.10.0 |
| Room | 2.7.2 |
| Hilt (Dagger) / androidx.hilt | 2.59.2 / 1.3.0 |
| Retrofit + Gson | 2.11.0 |
| minSdk / targetSdk / compileSdk | 24 / 36 / 37 |

Todas las versiones se declaran en `gradle/libs.versions.toml`.

## Cómo ejecutarlo

1. Clonar el repositorio: `git clone https://github.com/Yulioo/CarrerasAtletismo.git`
2. Abrir la carpeta en Android Studio y esperar a que sincronice Gradle.
3. Ejecutar en un emulador o en un celular con Android 7.0 (API 24) o superior, con la depuración USB activada.

Datos de maratones: RunDida (rundida.com).
