package io.github.budincsevity

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

internal interface ComicDownloader {
    @Headers("User-Agent: Mozilla/5.0", "Referer: readcomicbooksonline.net")
    @GET("{fileName}")
    fun downloadComic(@Path(value = "fileName", encoded = true) fileName: String): Call<ResponseBody>
}
