package com.proyecto.popayancultural.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.proyecto.popayancultural.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- MODEL WITH DESCRIPTION AND SIMULATED COMMENTS ---
data class GalleryPost(
    val id: Int,
    val imageUrl: String,
    val category: String,
    val authorName: String,
    val description: String,
    val likesCount: Int,
    val commentsCount: Int,
    val isAiRecommended: Boolean = false,
    val topComments: List<Pair<String, String>> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen() {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDetail by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<GalleryPost?>(null) }

    val tagsDB = listOf("All", "Holy Week", "Architecture", "Crafts", "Local Life")
    var tagActive by remember { mutableStateOf("All") }

    val baseDesc = "A piece that captures the unshakable essence of Cauca. Its textures and shadows reveal years of preserved tradition standing firm against the passage of time."
    val sampleComments = listOf(
        Pair("Ana P.", "Amazing how you captured that light."),
        Pair("Carlos G.", "Reminds me of my childhood downtown."),
        Pair("Luz M.", "A work worthy of a national exhibition.")
    )

    val dbPosts = remember {
        listOf(
            GalleryPost(1, "https://i.pinimg.com/1200x/17/d6/9f/17d69f8fae014ee87261ecb20a4702c2.jpg", "Holy Week", "Maestro Solarte", baseDesc, 124, 12, true, sampleComments),
            GalleryPost(2, "https://i.pinimg.com/736x/48/b1/9c/48b19cbac47db10be6e71d1fe4849b9a.jpg", "Architecture", "Ana López", "Facades that hold the echo of centuries past.", 89, 4, false, sampleComments.take(1)),
            GalleryPost(3, "https://i.pinimg.com/1200x/0f/41/9c/0f419c38373d6a579e7dfeb8fe09cfb1.jpg", "Crafts", "Taller Chamba", "Clay made into poetry. Manual work of more than 40 hours.", 342, 45, true, sampleComments),
            GalleryPost(4, "https://i.pinimg.com/736x/a0/40/9d/a0409d3b3de331b2c4c45cb2943db8ec.jpg", "Architecture", "Carlos Ruiz", baseDesc, 56, 2),
            GalleryPost(5, "https://i.pinimg.com/1200x/f0/e4/ab/f0e4abce10532cb8fd7943db5ff2bd40.jpg", "Holy Week", "Cofradía", "The absolute silence of the procession night.", 210, 18, false, sampleComments.take(2)),
            GalleryPost(6, "https://i.pinimg.com/1200x/bd/c9/91/bdc99164ee0dd710585ddc73cf2308b2.jpg", "Local Life", "Foto Popayán", baseDesc, 15, 0),
            GalleryPost(7, "https://i.pinimg.com/736x/1c/40/d0/1c40d04ec412ee8b7269fd16e9462b96.jpg", "Architecture", "Ana López", baseDesc, 430, 67, true, sampleComments),
            GalleryPost(8, "https://i.pinimg.com/1200x/61/29/2f/61292f01e939938b2b0103115100295e.jpg", "Crafts", "Maestro Solarte", "Filigree that imitates the nature of the highlands.", 112, 5, false, sampleComments.take(1)),
            GalleryPost(9, "https://i.pinimg.com/1200x/02/c7/9e/02c79ea838bd9506a93beff15f95ed7a.jpg", "Holy Week", "Historical Archive", baseDesc, 500, 120, false, sampleComments),
            GalleryPost(10, "https://i.pinimg.com/736x/c7/64/1d/c7641d938201e25293d85cd9f93d2cb4.jpg", "Architecture", "Carlos Ruiz", baseDesc, 88, 3),
            GalleryPost(11, "https://i.pinimg.com/1200x/a3/73/8c/a3738c1cfba8c83c3a92ed67aeb5119e.jpg", "Crafts", "Taller Chamba", baseDesc, 25, 1),
            GalleryPost(12, "https://i.pinimg.com/1200x/09/e7/6b/09e76b993810b9ed224dc945184616c1.jpg", "Local Life", "Foto Popayán", baseDesc, 67, 8, true),
            GalleryPost(13, "https://i.pinimg.com/736x/8b/3b/59/8b3b5933e3ff1f0220dafcaaac71a834.jpg", "Architecture", "Ana López", baseDesc, 32, 0),
            GalleryPost(14, "https://i.pinimg.com/1200x/ee/9f/7f/ee9f7fb6e91ac7f5952dce61fed8840f.jpg", "Holy Week", "Cofradía", baseDesc, 190, 22, false, sampleComments),
            GalleryPost(15, "https://i.pinimg.com/736x/9d/6a/11/9d6a11059dc7d6b30f6bb991f6018c2d.jpg", "Local Life", "Foto Popayán", baseDesc, 45, 2),
            GalleryPost(16, "https://i.pinimg.com/1200x/ae/33/c3/ae33c3125a84013394a19b8b500dd19a.jpg", "Crafts", "Maestro Solarte", baseDesc, 78, 4),
            GalleryPost(17, "https://i.pinimg.com/1200x/9c/a2/b8/9ca2b82a285189bd28f0d4ee8fdabdd5.jpg", "Architecture", "Carlos Ruiz", baseDesc, 150, 15, true, sampleComments.take(2)),
            GalleryPost(18, "https://i.pinimg.com/1200x/d5/6d/84/d56d84c8cfa1bec6cfdae6f2035757bf.jpg", "Holy Week", "Historical Archive", baseDesc, 340, 50),
            GalleryPost(19, "https://i.pinimg.com/736x/11/1d/2c/111d2cb145cd0349a272b423f59d255a.jpg", "Local Life", "Ana López", baseDesc, 21, 1),
            GalleryPost(20, "https://i.pinimg.com/1200x/37/38/25/37382553cfd6220c0fd840b2284f1c23.jpg", "Crafts", "Taller Chamba", baseDesc, 99, 9)
        )
    }

    val filteredPosts = if (tagActive == "All") dbPosts else dbPosts.filter { it.category == tagActive }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDeep)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("HERITAGE ARCHIVE", color = VioletAcento, style = MaterialTheme.typography.labelSmall, letterSpacing = 3.sp)
                    Text("GALLERY", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black, letterSpacing = (-2).sp)
                }
                Text("${filteredPosts.size} WORKS", color = Color.White.copy(0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // --- FILTERS ---
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tagsDB) { tag ->
                    val isSelected = tagActive == tag
                    Surface(
                        modifier = Modifier.clickable { tagActive = tag }.clip(RoundedCornerShape(12.dp)),
                        color = if (isSelected) VioletAcento else CardBackground,
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Text(
                            text = tag,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = if (isSelected) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- GRID ---
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalItemSpacing = 14.dp
            ) {
                itemsIndexed(filteredPosts, key = { _, post -> post.id }) { index, post ->
                    GlassGalleryCard(post = post, index = index) {
                        selectedPost = post
                        showDetail = true
                    }
                }
            }
        }

        // --- INTERACTIVE BOTTOM SHEET ---
        if (showDetail && selectedPost != null) {
            ModalBottomSheet(
                onDismissRequest = { showDetail = false },
                sheetState = sheetState,
                containerColor = CardBackground,
                dragHandle = { BottomSheetDefaults.DragHandle(color = VioletAcento) }
            ) {
                PostDetailSheet(selectedPost!!)
            }
        }
    }
}

// --- "POLAROID GLASSMORPHIC" CARD ---
@Composable
fun GlassGalleryCard(post: GalleryPost, index: Int, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    var showHeartAnim by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(index * 40L)
        visible = true
    }

    val scaleState by animateFloatAsState(if (isPressed) 0.94f else 1f, label = "Press")

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { 50 })
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scaleState)
                .clip(RoundedCornerShape(30.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(30.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onClick() },
                        onDoubleTap = {
                            coroutineScope.launch {
                                showHeartAnim = true
                                delay(800)
                                showHeartAnim = false
                            }
                        }
                    )
                }
        ) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )

            if (post.isAiRecommended) {
                Box(modifier = Modifier.align(Alignment.TopStart).padding(12.dp).background(VioletAcento, RoundedCornerShape(6.dp))) {
                    Text("★ FOR YOU", modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Black)
                }
            }

            AnimatedVisibility(
                visible = showHeartAnim,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioHighBouncy)),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(60.dp))
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.9f))))
                    .padding(12.dp)
            ) {
                Column {
                    Text(post.authorName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, null, tint = VioletAcento, modifier = Modifier.size(10.dp))
                        Text(" ${post.likesCount}", color = Color.White.copy(0.7f), fontSize = 9.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Outlined.ChatBubbleOutline, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(10.dp))
                        Text(" ${post.commentsCount}", color = Color.White.copy(0.7f), fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

// --- DETAIL: TABS IMPLEMENTATION ---
@Composable
fun PostDetailSheet(post: GalleryPost) {
    var isSaved by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(0) } // 0 = Visual Story, 1 = Community

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp)
    ) {
        // FEATURED IMAGE
        Box(modifier = Modifier.fillMaxWidth().height(350.dp).clip(RoundedCornerShape(30.dp))) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                color = VioletAcento,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(post.category, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ACTIONS
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            ActionIconButton(Icons.Default.FavoriteBorder, "Inspire", Color.White) {}
            ActionIconButton(Icons.Default.Refresh, "Repost", Color.White) {}
            ActionIconButton(
                if (isSaved) Icons.Default.Star else Icons.Default.StarBorder,
                if (isSaved) "Saved" else "Save",
                if (isSaved) VioletAcento else Color.White
            ) { isSaved = !isSaved }
            ActionIconButton(Icons.Default.Share, "Share", Color.White) {}
        }

        Spacer(modifier = Modifier.height(30.dp))

        // --- TABS SYSTEM ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(0.05f), RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            CustomTab(
                title = "VISUAL STORY",
                isSelected = currentTab == 0,
                modifier = Modifier.weight(1f)
            ) { currentTab = 0 }

            CustomTab(
                title = "COMMUNITY (${post.commentsCount})",
                isSelected = currentTab == 1,
                modifier = Modifier.weight(1f)
            ) { currentTab = 1 }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- DYNAMIC CONTENT ACCORDING TO TAB ---
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "TabContent"
        ) { targetTab ->
            if (targetTab == 0) {
                // VIEW: VISUAL STORY
                Column {
                    Text("AUTHOR: ${post.authorName.uppercase()}", color = VioletAcento, style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        post.description,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 24.sp,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "Impact: ${post.likesCount} inspirations generated.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            } else {
                // VIEW: COMMUNITY (COMMENTS)
                Column {
                    if (post.topComments.isNotEmpty()) {
                        post.topComments.forEach { (user, comment) ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                color = Color.Transparent,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color.White.copy(0.1f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(user, color = VioletAcento, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(comment, color = Color.White.copy(0.7f), fontSize = 14.sp)
                                }
                            }
                        }
                    } else {
                        Text("No comments yet. Be the first to share your thoughts.", color = Color.Gray, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fake comment input
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Write a comment...", color = Color.Gray, fontSize = 14.sp) },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.White.copy(0.1f),
                            focusedBorderColor = VioletAcento,
                            unfocusedContainerColor = CardBackground
                        )
                    )
                }
            }
        }
    }
}

// Custom Tab Button
@Composable
fun CustomTab(title: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) VioletAcento else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun ActionIconButton(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(52.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        }
        Text(label, modifier = Modifier.padding(top = 8.dp), color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}