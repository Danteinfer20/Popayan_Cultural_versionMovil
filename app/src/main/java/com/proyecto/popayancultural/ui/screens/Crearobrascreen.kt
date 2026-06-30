package com.proyecto.popayancultural.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.proyecto.popayancultural.data.RetrofitClient
import com.proyecto.popayancultural.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

// ─────────────────────────────────────────────────────────────
//  TIPOS
// ─────────────────────────────────────────────────────────────

enum class PostType { ART, PRODUCT }

// ─────────────────────────────────────────────────────────────
//  UI STATE
// ─────────────────────────────────────────────────────────────

data class CrearObraUiState(
    val postType        : PostType  = PostType.ART,
    val editingPostId   : Int?      = null,   // null = crear, !null = editar
    val existingImageUrl: String?   = null,   // imagen actual al editar
    val title           : String    = "",
    val content         : String    = "",
    val categoryId      : String    = "",
    val categories      : List<Pair<Int, String>> = emptyList(),
    val contentTypeId   : String    = "",
    val contentTypes    : List<Pair<Int, String>> = emptyList(),
    val imageUri        : Uri?      = null,
    val extraUri1       : Uri?      = null,
    val extraUri2       : Uri?      = null,
    val price           : String    = "",
    val stock           : String    = "",
    val productType     : String    = "physical",
    val isLoading       : Boolean   = false,
    val isSuccess       : Boolean   = false,
    val errorMessage    : String?   = null
)

// ─────────────────────────────────────────────────────────────
//  VIEW MODEL
// ─────────────────────────────────────────────────────────────

class CrearObraViewModel(app: android.app.Application) : AndroidViewModel(app) {
    private val _uiState = MutableStateFlow(CrearObraUiState())
    val uiState: StateFlow<CrearObraUiState> = _uiState.asStateFlow()

    init {
        loadCategories(PostType.ART)
        loadContentTypes()
    }

    fun loadPostForEdit(postId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = RetrofitClient.apiService.getPostDetalle(postId.toString())
                if (response.isSuccessful) {
                    val post = response.body()?.data ?: return@launch
                    _uiState.update { it.copy(
                        isLoading        = false,
                        editingPostId    = postId,
                        title            = post.title,
                        content          = post.content,
                        categoryId       = post.category?.id?.toString() ?: "",
                        existingImageUrl = post.imageUrl.ifBlank { null }
                    )}
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onTabChange(type: PostType) {
        _uiState.update { it.copy(postType = type, categoryId = "", categories = emptyList()) }
        loadCategories(type)
    }

    private fun loadCategories(type: PostType) {
        viewModelScope.launch {
            try {
                val catType = if (type == PostType.PRODUCT) "product" else "art"
                val response = RetrofitClient.apiService.getCategories(type = catType)
                if (response.isSuccessful) {
                    val cats = response.body()?.data?.map { it.id to (it.name ?: "") } ?: emptyList()
                    _uiState.update { it.copy(categories = cats) }
                }
            } catch (e: Exception) { }
        }
    }

    private fun loadContentTypes() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getContentTypes()
                if (response.isSuccessful) {
                    val types = response.body()?.data?.map { it.id to (it.name ?: "") } ?: emptyList()
                    _uiState.update { it.copy(contentTypes = types) }
                }
            } catch (e: Exception) { }
        }
    }

    fun onTitleChange(v: String)          = _uiState.update { it.copy(title = v) }
    fun onContentChange(v: String)        = _uiState.update { it.copy(content = v) }
    fun onCategoryChange(v: String)       = _uiState.update { it.copy(categoryId = v) }
    fun onContentTypeChange(v: String)    = _uiState.update { it.copy(contentTypeId = v) }
    fun onImageSelected(uri: Uri)         = _uiState.update { it.copy(imageUri = uri) }
    fun onExtraImage1Selected(uri: Uri)   = _uiState.update { it.copy(extraUri1 = uri) }
    fun onExtraImage2Selected(uri: Uri)   = _uiState.update { it.copy(extraUri2 = uri) }
    fun onPriceChange(v: String)          = _uiState.update { it.copy(price = v) }
    fun onStockChange(v: String)          = _uiState.update { it.copy(stock = v) }
    fun onProductTypeChange(v: String)    = _uiState.update { it.copy(productType = v) }
    fun clearError()                      = _uiState.update { it.copy(errorMessage = null) }

    fun publish() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El título es obligatorio") }; return
        }
        if (state.categoryId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Selecciona una categoría") }; return
        }
        if (state.contentTypeId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Selecciona la disciplina técnica") }; return
        }
        // En modo edición la imagen es opcional (ya tiene una)
        if (state.imageUri == null && state.editingPostId == null) {
            _uiState.update { it.copy(errorMessage = "Debes seleccionar una imagen") }; return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val ctx = getApplication<android.app.Application>().applicationContext

                fun String.toText() = toRequestBody("text/plain".toMediaTypeOrNull())

                fun uriToPart(uri: Uri, fieldName: String): MultipartBody.Part {
                    val inputStream = ctx.contentResolver.openInputStream(uri)
                    val file = File(ctx.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(file).use { out -> inputStream?.copyTo(out) }
                    return MultipartBody.Part.createFormData(
                        fieldName, file.name,
                        file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                }

                if (state.postType == PostType.PRODUCT) {
                    // Producto: hasta 3 imágenes como images[]
                    val imageParts = mutableListOf<MultipartBody.Part>()
                    state.imageUri?.let { imageParts.add(uriToPart(it, "images[]")) }
                    state.extraUri1?.let { imageParts.add(uriToPart(it, "images[]")) }
                    state.extraUri2?.let { imageParts.add(uriToPart(it, "images[]")) }

                    val response = RetrofitClient.apiService.createProduct(
                        name          = state.title.toText(),
                        description   = state.content.toText(),
                        categoryId    = state.categoryId.toText(),
                        contentTypeId = state.contentTypeId.toText(),
                        price         = state.price.ifBlank { "0" }.toText(),
                        stock         = state.stock.ifBlank { "0" }.toText(),
                        productType   = state.productType.toText(),
                        status        = "available".toText(),
                        images        = imageParts
                    )
                    if (response.isSuccessful) {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    } else {
                        val err = response.errorBody()?.string() ?: "Error desconocido"
                        _uiState.update { it.copy(isLoading = false, errorMessage = "[${response.code()}] $err") }
                    }
                } else {
                    // Obra: imagen singular como image (opcional en edición)
                    val imagePart = state.imageUri?.let { uriToPart(it, "image") }

                    val response = if (state.editingPostId != null) {
                        // PUT — actualizar obra existente
                        RetrofitClient.apiService.updatePost(
                            id            = state.editingPostId,
                            title         = state.title.toText(),
                            content       = state.content.toText(),
                            categoryId    = state.categoryId.toText(),
                            contentTypeId = state.contentTypeId.toText(),
                            method        = "PUT".toText(),
                            image         = imagePart
                        )
                    } else {
                        // POST — crear nueva obra
                        RetrofitClient.apiService.createPost(
                            title         = state.title.toText(),
                            content       = state.content.toText(),
                            categoryId    = state.categoryId.toText(),
                            contentTypeId = state.contentTypeId.toText(),
                            status        = "published".toText(),
                            image         = imagePart
                        )
                    }
                    if (response.isSuccessful) {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    } else {
                        val err = response.errorBody()?.string() ?: "Error desconocido"
                        _uiState.update { it.copy(isLoading = false, errorMessage = "[${response.code()}] $err") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error: ${e.message}") }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  SCREEN
// ─────────────────────────────────────────────────────────────

private val ArtistRed = Color(0xFFEF4444)

@Composable
fun CrearObraScreen(
    postId    : Int?           = null,
    onBack    : () -> Unit     = {},
    onSuccess : () -> Unit     = {},
    viewModel : CrearObraViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Cargar datos si es edición
    LaunchedEffect(postId) {
        if (postId != null) viewModel.loadPostForEdit(postId)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onSuccess()
    }

    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> uri?.let { viewModel.onImageSelected(it) } }

    val extraLauncher1 = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> uri?.let { viewModel.onExtraImage1Selected(it) } }

    val extraLauncher2 = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> uri?.let { viewModel.onExtraImage2Selected(it) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp).background(CardBackground, RoundedCornerShape(12.dp))
            ) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = Color.White) }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    if (uiState.postType == PostType.PRODUCT) "NUEVO PRODUCTO" else "NUEVA OBRA",
                    color = Color.White, fontSize = 18.sp,
                    fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic
                )
                Text(
                    if (uiState.postType == PostType.PRODUCT) "Añadir a tu tienda" else "Arte, artesanía o ilustración",
                    color = Color.Gray, fontSize = 11.sp
                )
            }
        }

        // ── Tabs OBRA / PRODUCTO ────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(PostType.ART to "OBRA", PostType.PRODUCT to "PRODUCTO").forEach { (type, label) ->
                val isActive = uiState.postType == type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isActive) ArtistRed else CardBackground)
                        .border(0.5.dp,
                            if (isActive) ArtistRed else Color.White.copy(0.08f),
                            RoundedCornerShape(20.dp))
                        .clickable { viewModel.onTabChange(type) }
                        .padding(horizontal = 20.dp, vertical = 9.dp)
                ) {
                    Text(label,
                        color = if (isActive) Color.White else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        letterSpacing = 0.5.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Imagen principal ────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CardBackground)
                .clickable {
                    imageLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (uiState.imageUri != null) {
                AsyncImage(
                    model = uiState.imageUri, contentDescription = null,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cambiar imagen", color = Color.White, fontSize = 12.sp)
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.AddPhotoAlternate, null,
                        tint = Color.Gray, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Toca para seleccionar imagen", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        // ── Imágenes extra (solo PRODUCTO) ──────────────────
        if (uiState.postType == PostType.PRODUCT) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    Triple(uiState.extraUri1, "VISTA 2") { extraLauncher1.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    Triple(uiState.extraUri2, "VISTA 3") { extraLauncher2.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                ).forEach { (uri, label, onClick) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardBackground)
                            .border(0.5.dp, Color.White.copy(0.06f), RoundedCornerShape(14.dp))
                            .clickable { onClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uri != null) {
                            AsyncImage(
                                model = uri, contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Edit, null,
                                    tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.Add, null,
                                    tint = Color.Gray, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.height(4.dp))
                                Text(label, color = Color.Gray, fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Formulario ──────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // Título
            CreatorField(if (uiState.postType == PostType.PRODUCT) "NOMBRE DEL PRODUCTO" else "TÍTULO") {
                OutlinedTextField(
                    value = uiState.title, onValueChange = viewModel::onTitleChange,
                    placeholder = { Text(
                        if (uiState.postType == PostType.PRODUCT) "Nombre del producto..." else "Nombre de la obra...",
                        color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = creatorFieldColors(), singleLine = true
                )
            }

            // Categorías
            CreatorField("CATEGORÍA") {
                if (uiState.categories.isEmpty()) {
                    CircularProgressIndicator(color = ArtistRed,
                        modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.categories.size) { idx ->
                            val (id, name) = uiState.categories[idx]
                            val isSelected = uiState.categoryId == id.toString()
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) ArtistRed else CardBackground)
                                    .border(0.5.dp,
                                        if (isSelected) ArtistRed else Color.White.copy(0.08f),
                                        RoundedCornerShape(20.dp))
                                    .clickable { viewModel.onCategoryChange(id.toString()) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(name,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }

            // ── DISCIPLINA TÉCNICA (content_type_id) — ambos tipos ──
            CreatorField("DISCIPLINA TÉCNICA / FORMATO") {
                if (uiState.contentTypes.isEmpty()) {
                    CircularProgressIndicator(color = ArtistRed,
                        modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.contentTypes.size) { idx ->
                            val (id, name) = uiState.contentTypes[idx]
                            val isSelected = uiState.contentTypeId == id.toString()
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) ArtistRed else CardBackground)
                                    .border(0.5.dp,
                                        if (isSelected) ArtistRed else Color.White.copy(0.08f),
                                        RoundedCornerShape(20.dp))
                                    .clickable { viewModel.onContentTypeChange(id.toString()) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(name,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }

            // ── Campos extra PRODUCTO ───────────────────────
            if (uiState.postType == PostType.PRODUCT) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        CreatorField("PRECIO (COP)") {
                            OutlinedTextField(
                                value = uiState.price, onValueChange = viewModel::onPriceChange,
                                placeholder = { Text("0", color = Color.Gray, fontSize = 13.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = creatorFieldColors(), singleLine = true
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        CreatorField("STOCK") {
                            OutlinedTextField(
                                value = uiState.stock, onValueChange = viewModel::onStockChange,
                                placeholder = { Text("0", color = Color.Gray, fontSize = 13.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = creatorFieldColors(), singleLine = true
                            )
                        }
                    }
                }

                CreatorField("TIPO DE PRODUCTO") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("physical" to "Físico", "digital" to "Digital").forEach { (id, label) ->
                            val isSelected = uiState.productType == id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) ArtistRed else CardBackground)
                                    .border(0.5.dp,
                                        if (isSelected) ArtistRed else Color.White.copy(0.08f),
                                        RoundedCornerShape(12.dp))
                                    .clickable { viewModel.onProductTypeChange(id) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(label,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }

            // Descripción
            CreatorField(if (uiState.postType == PostType.PRODUCT) "DESCRIPCIÓN" else "DESCRIPCIÓN / RELATO") {
                OutlinedTextField(
                    value = uiState.content, onValueChange = viewModel::onContentChange,
                    placeholder = { Text(
                        if (uiState.postType == PostType.PRODUCT) "Describe el producto..."
                        else "Cuéntale al mundo sobre esta obra...",
                        color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = creatorFieldColors(), maxLines = 6
                )
            }

            // Error
            uiState.errorMessage?.let { msg ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(0.1f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Warning, null,
                            tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(msg, color = Color(0xFFEF4444), fontSize = 12.sp)
                    }
                }
            }

            // Botón publicar
            Button(
                onClick = { viewModel.publish() },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ArtistRed)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White,
                        modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Publicando...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                } else {
                    Icon(Icons.Outlined.Upload, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (uiState.editingPostId != null) "GUARDAR CAMBIOS"
                        else if (uiState.postType == PostType.PRODUCT) "PUBLICAR PRODUCTO"
                        else "PUBLICAR OBRA",
                        fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  HELPERS
// ─────────────────────────────────────────────────────────────

@Composable
private fun CreatorField(label: String, content: @Composable () -> Unit) {
    Column {
        Text(
            label, color = Color.Gray, fontSize = 9.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        content()
    }
}

@Composable
private fun creatorFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = CardBackground,
    unfocusedContainerColor = CardBackground,
    focusedBorderColor      = ArtistRed,
    unfocusedBorderColor    = Color.White.copy(alpha = 0.06f),
    focusedTextColor        = Color.White,
    unfocusedTextColor      = Color.White
)