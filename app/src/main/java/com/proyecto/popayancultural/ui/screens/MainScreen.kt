package com.proyecto.popayancultural.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.proyecto.popayancultural.data.ApiService
import com.proyecto.popayancultural.data.RetrofitClient
import com.proyecto.popayancultural.data.repository.ComprasRepository
import com.proyecto.popayancultural.data.repository.FavoritosRepository
import com.proyecto.popayancultural.ui.AuthRole
import com.proyecto.popayancultural.ui.screens.agenda.AgendaScreen
import com.proyecto.popayancultural.ui.screens.agenda.DetalleEventoScreen
import com.proyecto.popayancultural.ui.viewmodels.ProfileViewModel
import com.proyecto.popayancultural.ui.viewmodels.UserDashboardViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private val BgBase      = Color(0xFF080808)
private val NavBg       = Color(0xFF0D0D10)
private val NavBorder   = Color(0xFF1A1A22)
private val NavSelected = Color(0xFF18102A)
private val Violet      = Color(0xFFA855F7)
private val NavActive   = Color(0xFFA855F7)
private val NavInactive = Color(0xFF444448)
private val TextPrimary = Color(0xFFF0F0F0)

private object Routes {
    const val HOME              = "home"
    const val AGENDA            = "agenda"
    const val EXPLORA           = "explora"
    const val TIENDA            = "tienda"
    const val PERFIL            = "perfil"
    const val SETTINGS          = "settings"
    const val USER_DASHBOARD    = "user_dashboard"
    const val ARTIST_DASHBOARD  = "artist_dashboard"
    const val MIS_OBRAS         = "mis_obras"
    const val MI_TIENDA         = "mi_tienda"
    const val VENTAS            = "ventas"
    const val CREAR_OBRA        = "crear_obra"
    const val EDITAR_OBRA       = "editar_obra/{postId}"
    const val DETALLE_EVENTO    = "detalle_evento/{eventId}"
    const val DETALLE_OBRA      = "detalle_obra/{slug}"
    const val DETALLE_PRODUCTO  = "detalle_producto/{productId}"
    const val DETALLE_ARTISTA   = "detalle_artista/{username}"
    const val DETALLE_EDUCACION = "detalle_educacion/{educacionId}"
    const val FAVORITOS         = "favoritos"
    const val COMPRAS           = "compras"

    fun detalleEvento(id: Int)           = "detalle_evento/$id"
    fun detalleObra(slug: String)        = "detalle_obra/$slug"
    fun detalleProducto(id: Int)         = "detalle_producto/$id"
    fun editarObra(id: Int)              = "editar_obra/$id"
    fun detalleArtista(username: String) = "detalle_artista/$username"
    fun detalleEducacion(id: Int)        = "detalle_educacion/$id"
}

sealed class BottomNavItem(
    val route         : String,
    val selectedIcon  : ImageVector,
    val unselectedIcon: ImageVector,
    val label         : String,
    val badgeCount    : Int = 0
) {
    object Home   : BottomNavItem(Routes.HOME,    Icons.Filled.Home,        Icons.Outlined.Home,        "Inicio")
    object Agenda : BottomNavItem(Routes.AGENDA,  Icons.Filled.DateRange,   Icons.Outlined.DateRange,   "Agenda")
    object Explora: BottomNavItem(Routes.EXPLORA, Icons.Filled.Explore,     Icons.Outlined.Explore,     "Explora")
    object Tienda : BottomNavItem(Routes.TIENDA,  Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag, "Tienda")
    object Perfil : BottomNavItem(Routes.PERFIL,  Icons.Filled.Person,      Icons.Outlined.Person,      "Perfil")
}

private val routesWithoutNavBar = setOf(
    Routes.DETALLE_EVENTO,
    Routes.DETALLE_OBRA,
    Routes.DETALLE_PRODUCTO,
    Routes.DETALLE_ARTISTA,
    Routes.DETALLE_EDUCACION,
    Routes.SETTINGS,
    Routes.USER_DASHBOARD,
    Routes.ARTIST_DASHBOARD,
    Routes.MIS_OBRAS,
    Routes.MI_TIENDA,
    Routes.VENTAS,
    Routes.CREAR_OBRA,
    Routes.EDITAR_OBRA,
    Routes.FAVORITOS,
    Routes.COMPRAS
)

// ─── ViewModel auxiliar para cargar producto por ID desde la API ──────────────
// Se usa cuando se navega a DetalleProducto desde Home (Tienda no está en el backstack)
class ProductoByIdViewModel : ViewModel() {
    private val _product = MutableStateFlow<com.proyecto.popayancultural.data.models.Product?>(null)
    val product: StateFlow<com.proyecto.popayancultural.data.models.Product?> = _product

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    fun cargar(productId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.apiService.getProducts(limit = 100)
                if (response.isSuccessful) {
                    _product.value = response.body()?.data?.find { it.id == productId }
                }
            } catch (_: Exception) { }
            finally { _loading.value = false }
        }
    }
}

@Composable
fun MainScreen(
    onLogout   : () -> Unit,
    isLoggedIn : Boolean  = false,
    initialRole: AuthRole = AuthRole.VISITOR,
    apiService : ApiService
) {
    val navController  = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route
    val showNavBar     = currentRoute !in routesWithoutNavBar

    val profileViewModel: ProfileViewModel = viewModel()

    val userDashboardViewModel: UserDashboardViewModel = viewModel(
        factory = UserDashboardViewModel.Factory(
            favoritosRepository = FavoritosRepository(apiService),
            comprasRepository   = ComprasRepository(apiService)
        )
    )

    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Agenda,
        BottomNavItem.Explora,
        BottomNavItem.Tienda,
        BottomNavItem.Perfil
    )

    fun navigateTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState    = true
        }
    }

    Scaffold(
        containerColor = BgBase,
        bottomBar = {
            if (showNavBar) {
                CleanNavBar(
                    items        = navItems,
                    currentRoute = currentRoute,
                    onNavigate   = ::navigateTab
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Routes.HOME,
            modifier         = Modifier.padding(innerPadding)
        ) {

            composable(Routes.HOME) {
                HomeScreen(
                    onEventClick   = { eventId  -> navController.navigate(Routes.detalleEvento(eventId)) },
                    onObraClick    = { slug      -> navController.navigate(Routes.detalleObra(slug)) },
                    onProductClick = { productId -> navController.navigate(Routes.detalleProducto(productId)) },
                    onArtistaClick = { username  -> navController.navigate(Routes.detalleArtista(username)) },
                    onEducaClick   = { id        -> navController.navigate(Routes.detalleEducacion(id)) }
                )
            }

            composable(Routes.AGENDA) {
                AgendaScreen(
                    onEventClick = { eventId ->
                        navController.navigate(Routes.detalleEvento(eventId))
                    }
                )
            }

            composable(Routes.EXPLORA) {
                ExploraScreen(
                    onObraClick      = { slug     -> navController.navigate(Routes.detalleObra(slug)) },
                    onArtistaClick   = { username -> navController.navigate(Routes.detalleArtista(username)) },
                    onEducacionClick = { id       -> navController.navigate(Routes.detalleEducacion(id)) }
                )
            }

            composable(Routes.TIENDA) {
                TiendaScreen(
                    isLoggedIn     = isLoggedIn,
                    onProductClick = { productId ->
                        navController.navigate(Routes.detalleProducto(productId))
                    }
                )
            }

            composable(Routes.PERFIL) {
                ProfileScreen(
                    viewModel                   = profileViewModel,
                    dashboardViewModel          = userDashboardViewModel,
                    onNavigateToUserDashboard   = { navController.navigate(Routes.USER_DASHBOARD)   { launchSingleTop = true } },
                    onNavigateToArtistDashboard = { navController.navigate(Routes.ARTIST_DASHBOARD) { launchSingleTop = true } },
                    onNavigateToSettings        = { navController.navigate(Routes.SETTINGS)         { launchSingleTop = true } },
                    onNavigateToCrearObra = { navController.navigate(Routes.CREAR_OBRA) { launchSingleTop = true } },
                    onNavigateToMiTienda  = { navController.navigate(Routes.MI_TIENDA)  { launchSingleTop = true } },
                    onNavigateToMisObras  = { navController.navigate(Routes.MIS_OBRAS)  { launchSingleTop = true } },
                    onNavigateToVentas    = { navController.navigate(Routes.VENTAS)     { launchSingleTop = true } },
                    onLogout              = onLogout
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    viewModel = profileViewModel,
                    onBack    = { navController.popBackStack() }
                )
            }

            composable(Routes.USER_DASHBOARD) {
                val uiState by profileViewModel.uiState.collectAsState()
                UserDashboardScreen(
                    profile               = uiState.profile,
                    onNavigateToFavorites = { navController.navigate(Routes.FAVORITOS) { launchSingleTop = true } },
                    onNavigateToPurchases = { navController.navigate(Routes.COMPRAS)   { launchSingleTop = true } },
                    onNavigateToSettings  = { navController.navigate(Routes.SETTINGS)  { launchSingleTop = true } },
                    onApplyAsCreator      = { }
                )
            }

            composable(Routes.FAVORITOS) {
                FavoritosScreen(
                    viewModel        = userDashboardViewModel,
                    onNavigateToObra = { obraId ->
                        navController.navigate(Routes.detalleObra(obraId.toString()))
                    }
                )
            }

            composable(Routes.COMPRAS) {
                ComprasScreen(viewModel = userDashboardViewModel)
            }

            composable(Routes.ARTIST_DASHBOARD) {
                val uiState by profileViewModel.uiState.collectAsState()
                ArtistDashboardScreen(
                    artistName           = uiState.profile.name,
                    artistAvatar         = uiState.profile.profilePicture,
                    onNavigateToGallery  = { navController.navigate(Routes.MIS_OBRAS)  { launchSingleTop = true } },
                    onNavigateToStore    = { navController.navigate(Routes.MI_TIENDA)  { launchSingleTop = true } },
                    onNavigateToCreate   = { navController.navigate(Routes.CREAR_OBRA) { launchSingleTop = true } },
                    onNavigateToSales    = { navController.navigate(Routes.VENTAS)     { launchSingleTop = true } },
                    onNavigateToSettings = { navController.navigate(Routes.SETTINGS)   { launchSingleTop = true } }
                )
            }

            composable(Routes.MIS_OBRAS) {
                MisObrasScreen(
                    onBack       = { navController.popBackStack() },
                    onCrearObra  = { navController.navigate(Routes.CREAR_OBRA) { launchSingleTop = true } },
                    onEditarObra = { postId -> navController.navigate(Routes.editarObra(postId)) }
                )
            }

            composable(Routes.MI_TIENDA) {
                MiTiendaScreen(
                    onBack      = { navController.popBackStack() },
                    onCrearObra = { navController.navigate(Routes.CREAR_OBRA) { launchSingleTop = true } }
                )
            }

            composable(Routes.VENTAS) {
                VentasScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.CREAR_OBRA) {
                CrearObraScreen(
                    onBack    = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }

            composable(
                route     = Routes.EDITAR_OBRA,
                arguments = listOf(navArgument("postId") { type = NavType.IntType })
            ) { back ->
                val postId = back.arguments?.getInt("postId") ?: return@composable
                CrearObraScreen(
                    postId    = postId,
                    onBack    = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }

            composable(
                route     = Routes.DETALLE_EVENTO,
                arguments = listOf(navArgument("eventId") { type = NavType.IntType })
            ) { back ->
                val eventId = back.arguments?.getInt("eventId") ?: return@composable
                DetalleEventoScreen(
                    eventId          = eventId,
                    isLoggedIn       = isLoggedIn,
                    onBack           = { navController.popBackStack() },
                    onOrganizerClick = { }
                )
            }

            composable(
                route     = Routes.DETALLE_OBRA,
                arguments = listOf(navArgument("slug") { type = NavType.StringType })
            ) { back ->
                val slug = back.arguments?.getString("slug") ?: return@composable
                DetalleObraScreen(
                    slug         = slug,
                    isLoggedIn   = isLoggedIn,
                    onBack       = { navController.popBackStack() },
                    onAutorClick = { }
                )
            }

            composable(
                route     = Routes.DETALLE_ARTISTA,
                arguments = listOf(navArgument("username") { type = NavType.StringType })
            ) { back ->
                val username = back.arguments?.getString("username") ?: return@composable
                DetalleArtistaScreen(
                    username       = username,
                    onBack         = { navController.popBackStack() },
                    onObraClick    = { slug -> navController.navigate(Routes.detalleObra(slug)) },
                    onProductClick = { id   -> navController.navigate(Routes.detalleProducto(id)) }
                )
            }

            composable(
                route     = Routes.DETALLE_EDUCACION,
                arguments = listOf(navArgument("educacionId") { type = NavType.IntType })
            ) { back ->
                val id = back.arguments?.getInt("educacionId") ?: return@composable
                DetalleEducacionScreen(
                    id     = id,
                    onBack = { navController.popBackStack() }
                )
            }

            // ── DETALLE PRODUCTO ──────────────────────────────────────────────
            // Puede llegar desde Tienda (Tienda en backstack → usa su VM)
            // o desde Home/DetalleArtista (Tienda NO en backstack → carga por ID desde API)
            composable(
                route     = Routes.DETALLE_PRODUCTO,
                arguments = listOf(navArgument("productId") { type = NavType.IntType })
            ) { back ->
                val productId = back.arguments?.getInt("productId") ?: return@composable

                // Intentar obtener el VM de Tienda si está en el backstack
                val tiendaBackEntry = remember(back) {
                    runCatching { navController.getBackStackEntry(Routes.TIENDA) }.getOrNull()
                }

                if (tiendaBackEntry != null) {
                    // Camino normal: viene desde TiendaScreen
                    val tiendaViewModel: TiendaViewModel = viewModel(tiendaBackEntry)
                    val product = tiendaViewModel.getProductById(productId)

                    if (product != null) {
                        DetalleProductoScreen(
                            product   = product,
                            onBack    = { navController.popBackStack() },
                            onAgregar = {
                                tiendaViewModel.agregarYAbrirCarrito(product)
                                navController.popBackStack()
                            }
                        )
                    } else {
                        // El VM existe pero aún no tiene datos (carga async) → fallback
                        ProductoByIdFallback(
                            productId  = productId,
                            onBack     = { navController.popBackStack() },
                            onAgregar  = { prod ->
                                tiendaViewModel.agregarYAbrirCarrito(prod)
                                navController.popBackStack()
                            }
                        )
                    }
                } else {
                    // Camino desde Home / DetalleArtista: Tienda no está en el backstack
                    ProductoByIdFallback(
                        productId = productId,
                        onBack    = { navController.popBackStack() },
                        onAgregar = { _ -> navController.popBackStack() }
                    )
                }
            }
        }
    }
}

// ─── Fallback: carga el producto por ID directamente desde la API ─────────────
@Composable
private fun ProductoByIdFallback(
    productId : Int,
    onBack    : () -> Unit,
    onAgregar : (com.proyecto.popayancultural.data.models.Product) -> Unit
) {
    val vm: ProductoByIdViewModel = viewModel()
    val product by vm.product.collectAsState()
    val loading by vm.loading.collectAsState()

    LaunchedEffect(productId) { vm.cargar(productId) }

    when {
        loading -> {
            Box(
                modifier         = Modifier.fillMaxSize().background(Color(0xFF080808)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFA855F7))
            }
        }
        product != null -> {
            DetalleProductoScreen(
                product   = product!!,
                onBack    = onBack,
                onAgregar = { onAgregar(product!!) }
            )
        }
        else -> {
            // Producto no encontrado → volver atrás silenciosamente
            LaunchedEffect(Unit) { onBack() }
        }
    }
}

@Composable
private fun CleanNavBar(
    items       : List<BottomNavItem>,
    currentRoute: String?,
    onNavigate  : (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 20.dp, top = 8.dp)
    ) {
        Surface(
            modifier        = Modifier.fillMaxWidth().height(64.dp),
            shape           = RoundedCornerShape(20.dp),
            color           = NavBg,
            border          = androidx.compose.foundation.BorderStroke(0.5.dp, NavBorder),
            tonalElevation  = 0.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier              = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    CleanNavItem(
                        item       = item,
                        isSelected = currentRoute == item.route,
                        onClick    = { onNavigate(item.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CleanNavItem(item: BottomNavItem, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue   = if (isSelected) 1.05f else 1f,
        animationSpec = tween(180),
        label         = "scale_${item.route}"
    )
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) NavSelected else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier            = Modifier.scale(scale)
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector        = if (isSelected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                    tint               = if (isSelected) NavActive else NavInactive,
                    modifier           = Modifier.size(22.dp)
                )
                if (item.badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .offset(x = 5.dp, y = (-3).dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDC2626)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = if (item.badgeCount > 9) "9+" else "${item.badgeCount}",
                            color      = Color.White,
                            fontSize   = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text          = item.label,
                color         = if (isSelected) NavActive else NavInactive,
                fontSize      = 9.sp,
                fontWeight    = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                letterSpacing = 0.2.sp
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier         = Modifier.fillMaxSize().background(BgBase),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = TextPrimary.copy(alpha = 0.25f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Text("Próximamente", color = Violet.copy(alpha = 0.4f), fontSize = 12.sp)
        }
    }
}