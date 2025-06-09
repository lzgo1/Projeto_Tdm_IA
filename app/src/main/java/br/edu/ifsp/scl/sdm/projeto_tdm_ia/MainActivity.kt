package br.edu.ifsp.scl.sdm.projeto_tdm_ia

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.scl.sdm.projeto_tdm_ia.databinding.ActivityMainBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val apiKey = "SUA_CHAVE_DE_API_AQUI"
    private val apiUrl = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=$apiKey"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEnviar.setOnClickListener {
            val pergunta = binding.etPergunta.text.toString()
            if (pergunta.isNotEmpty()) {
                buscarResposta(pergunta)
            } else {
                Toast.makeText(this, "Digite uma pergunta!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buscarResposta(pergunta: String) {
        val client = OkHttpClient()
        val requestBody = """
            {
                "contents": [
                    {
                        "role": "user",
                        "parts": [
                            {"text": "$pergunta"}
                        ]
                    }
                ]
            }
        """.trimIndent()

        // Utiliza a função de extensão para criar o RequestBody
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestBody.toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Erro na requisição", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val jsonResponse = JSONObject(responseBody.string())
                    val resposta = jsonResponse.optJSONArray("candidates")
                        ?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text")

                    runOnUiThread {
                        binding.tvResposta.text = resposta ?: "Erro ao obter resposta"
                    }
                }
            }
        })
    }
}
