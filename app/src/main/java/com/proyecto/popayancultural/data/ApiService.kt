package com.proyecto.popayancultural.data

import com.proyecto.popayancultural.data.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==========================================
    // 🌐 EXPLORA / POSTS
    // ==========================================

    @GET("api/v1/posts")
    suspend fun getPosts(
        @Query("limit")   limit  : Int  = 20,
        @Query("user_id") userId : Int? = null
    ): Response<PostResponse>

    @GET("api/v1/posts")
    suspend fun getObras(
        @Query("type")    type   : String = "obra",
        @Query("limit")   limit  : Int    = 10,
        @Query("user_id") userId : Int?   = null
    ): Response<PostResponse>

    @GET("api/v1/posts")
    suspend fun getMyPosts(
        @Query("my_posts") myPosts: Boolean = true,
        @Query("limit")    limit  : Int     = 20
    ): Response<PostResponse>

    @GET("api/v1/posts/{identifier}")
    suspend fun getPostDetalle(
        @Path("identifier") identifier: String
    ): Response<PostDetailResponse>

    @Multipart
    @POST("api/v1/posts")
    suspend fun createPost(
        @Part("title")            title         : RequestBody,
        @Part("content")          content       : RequestBody,
        @Part("category_id")      categoryId    : RequestBody,
        @Part("content_type_id")  contentTypeId : RequestBody,
        @Part("status")           status        : RequestBody,
        @Part image               : MultipartBody.Part? = null
    ): Response<PostDetailResponse>

    @Multipart
    @POST("api/v1/posts/{id}")
    suspend fun updatePost(
        @Path("id")               id            : Int,
        @Part("title")            title         : RequestBody,
        @Part("content")          content       : RequestBody,
        @Part("category_id")      categoryId    : RequestBody,
        @Part("content_type_id")  contentTypeId : RequestBody,
        @Part("_method")          method        : RequestBody,
        @Part image               : MultipartBody.Part? = null
    ): Response<PostDetailResponse>

    @DELETE("api/v1/posts/{id}")
    suspend fun deletePost(@Path("id") id: Int): Response<GenericResponse>

    // ==========================================
    // 🎨 ARTISTAS
    // ==========================================

    @GET("api/v1/artists")
    suspend fun getArtists(
        @Query("limit") limit: Int = 10
    ): Response<ArtistResponse>

    @GET("api/v1/artists/{username}")
    suspend fun getArtistByUsername(
        @Path("username") username: String
    ): Response<ArtistDetailResponse>

    // ==========================================
    // 🎨 ARTIST DASHBOARD
    // ==========================================

    @GET("api/v1/artist/dashboard")
    suspend fun getArtistDashboard(): Response<ArtistDashboardResponse>

    // ==========================================
    // 📚 EDUCACIÓN
    // ==========================================

    @GET("api/v1/education")
    suspend fun getEducation(
        @Query("limit") limit: Int = 10
    ): Response<EducationResponse>

    @GET("api/v1/education/{id}")
    suspend fun getEducationById(
        @Path("id") id: Int
    ): Response<EducationDetailResponse>

    // ==========================================
    // 📅 EVENTOS
    // ==========================================

    @GET("api/v1/events")
    suspend fun getEvents(@Query("limit") limit: Int? = null): Response<EventResponse>

    @GET("api/v1/events/{id}")
    suspend fun getEventDetail(@Path("id") id: Int): Response<EventDetailResponse>

    @POST("api/v1/events/{id}/attend")
    suspend fun attend(@Path("id") id: Int): Response<AttendResponse>

    // ==========================================
    // 🛍️ PRODUCTOS
    // ==========================================

    @GET("api/v1/products")
    suspend fun getProducts(
        @Query("limit")       limit      : Int?     = null,
        @Query("user_id")     userId     : Int?     = null,
        @Query("my_products") myProducts : Boolean? = null
    ): Response<ProductResponse>

    @PUT("api/v1/products/{id}")
    suspend fun updateProductStatus(
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<GenericResponse>

    @DELETE("api/v1/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<GenericResponse>

    @Multipart
    @POST("api/v1/products")
    suspend fun createProduct(
        @Part("name")             name          : RequestBody,
        @Part("description")      description   : RequestBody,
        @Part("category_id")      categoryId    : RequestBody,
        @Part("content_type_id")  contentTypeId : RequestBody,
        @Part("price")            price         : RequestBody,
        @Part("stock_quantity")   stock         : RequestBody,
        @Part("product_type")     productType   : RequestBody,
        @Part("status")           status        : RequestBody,
        @Part images              : List<MultipartBody.Part>
    ): Response<ProductResponse>

    // ==========================================
    // 📂 CATEGORÍAS
    // ==========================================

    @GET("api/v1/categories")
    suspend fun getCategories(
        @Query("type") type: String? = null
    ): Response<CategoryResponse>

    @GET("api/v1/content-types")
    suspend fun getContentTypes(): Response<ContentTypeResponse>

    // ==========================================
    // 🔐 AUTENTICACIÓN
    // ==========================================

    @POST("api/v1/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @POST("api/v1/register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @POST("api/v1/forgot-password")
    fun recoverPassword(@Body request: Map<String, String>): Call<AuthResponse>

    // ==========================================
    // 👤 PERFIL
    // ==========================================

    @GET("api/v1/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @Multipart
    @POST("api/v1/profile/update")
    suspend fun updateProfile(
        @Part("name")         name        : RequestBody,
        @Part("bio")          bio         : RequestBody,
        @Part("phone")        phone       : RequestBody,
        @Part("city")         city        : RequestBody,
        @Part("neighborhood") neighborhood: RequestBody,
        @Part("website")      website     : RequestBody,
        @Part("social_media") socialMedia : RequestBody,
        @Part profilePicture : MultipartBody.Part? = null,
        @Part coverPicture   : MultipartBody.Part? = null
    ): Response<ProfileResponse>

    @POST("api/v1/profile/settings")
    suspend fun updateSettings(
        @Body request: UpdateSettingsRequest
    ): Response<GenericResponse>

    // ==========================================
    // 🔒 ACCIONES PROTEGIDAS
    // ==========================================

    @POST("api/v1/reactions/toggle")
    suspend fun toggleReaccion(
        @Body request: ToggleReactionRequest
    ): Response<Any>

    @POST("api/v1/saved-items/toggle")
    suspend fun toggleSaved(
        @Body request: ToggleSavedRequest
    ): Response<Any>

    @GET("api/v1/saved-items")
    suspend fun getSavedItems(): Response<SavedItemsResponse>

    @POST("api/v1/posts/{id}/share")
    suspend fun sharePost(@Path("id") id: Int): Response<Any>

    @POST("api/v1/comments")
    suspend fun crearComentario(
        @Body request: CreateCommentRequest
    ): Response<CommentResponse>

    // ==========================================
    // 🛒 ÓRDENES / TIENDA
    // ==========================================

    @POST("api/v1/orders")
    suspend fun crearOrden(
        @Body request: CreateOrderRequest
    ): Response<OrderResponse>

    @GET("api/v1/my-purchases")
    suspend fun misOrdenes(): Response<OrdersListResponse>

    @GET("api/v1/my-sales")
    suspend fun getMySales(): Response<OrdersListResponse>

    @PUT("api/v1/orders/{id}/confirm")
    suspend fun confirmarOrden(@Path("id") id: Int): Response<GenericResponse>
}