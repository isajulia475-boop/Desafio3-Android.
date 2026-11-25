
package com.example.desafio3

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var mensagem: TextView

    companion object {
        private const val CODIGO_NOTIFICACAO = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        criarTela()
    }

    private fun criarTela() {

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.rgb(245, 243, 250))
        }

        val titulo = TextView(this).apply {
            text = "Sincronização de Dados"
            textSize = 25f
            setTextColor(Color.rgb(65, 50, 100))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        val descricao = TextView(this).apply {
            text = "Serviço em primeiro plano com contador de pacotes"
            textSize = 16f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        }

        mensagem = TextView(this).apply {
            text = "Serviço aguardando início"
            textSize = 15f
            setTextColor(Color.rgb(65, 50, 100))
            gravity = Gravity.CENTER
        }

        val botaoIniciar = Button(this).apply {
            text = "Iniciar sincronização"

            setOnClickListener {
                verificarPermissaoNotificacao()
            }
        }

        val botaoParar = Button(this).apply {
            text = "Parar sincronização"

            setOnClickListener {
                pararSincronizacao()
            }
        }

        layout.addView(titulo)

        layout.addView(
            descricao,
            parametros(24, 24)
        )

        layout.addView(
            mensagem,
            parametros(8, 24)
        )

        layout.addView(botaoIniciar)

        layout.addView(
            botaoParar,
            parametros(16, 0)
        )

        setContentView(layout)
    }

    private fun parametros(
        margemSuperior: Int,
        margemInferior: Int
    ): LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = margemSuperior
            bottomMargin = margemInferior
        }
    }

    private fun verificarPermissaoNotificacao() {

        if (
            Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                CODIGO_NOTIFICACAO
            )

        } else {

            iniciarSincronizacao()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == CODIGO_NOTIFICACAO) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {

                iniciarSincronizacao()

            } else {

                mensagem.text =
                    "Permita as notificações nas configurações do aplicativo."
            }
        }
    }

    private fun iniciarSincronizacao() {

        val intent = Intent(
            this,
            SincronizacaoService::class.java
        )

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }

            mensagem.text = "Sincronização iniciada!"

        } catch (erro: Exception) {

            mensagem.text =
                "Não foi possível iniciar: ${erro.message}"
        }
    }

    private fun pararSincronizacao() {

        val intent = Intent(
            this,
            SincronizacaoService::class.java
        )

        stopService(intent)

        mensagem.text = "Sincronização encerrada."
    }
}
