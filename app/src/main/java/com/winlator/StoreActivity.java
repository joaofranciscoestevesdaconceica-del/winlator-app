package com.winlator;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class StoreActivity extends Activity {
    private LinearLayout layoutProgress;
    private ProgressBar progressBar;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        layoutProgress = findViewById(R.id.layout_progress);
        progressBar = findViewById(R.id.progress_bar_download);
        txtStatus = findViewById(R.id.txt_status_download);
    }

    // Método que o botão da loja vai chamar para baixar o Windows, Mac ou Linux
    public void baixarContainer(String urlDownload, String nomeSistema) {
        new DownloadTask(nomeSistema).execute(urlDownload);
    }

    // Motor assíncrono que faz o download calculando a porcentagem em tempo real
    private class DownloadTask extends AsyncTask<String, Integer, String> {
        private String nomeSistema;

        public DownloadTask(String nomeSistema) {
            this.nomeSistema = nomeSistema;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            txtStatus.setText("Iniciando download de: " + nomeSistema);
            layoutProgress.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conexao = url.openConnection();
                conexao.connect();

                // Calcula o tamanho total do arquivo na nuvem do GitHub
                int tamanhoArquivo = conexao.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                File destino = new File(getFilesDir() + "/containers/" + nomeSistema + ".qcow2");
                destino.getParentFile().mkdirs();

                FileOutputStream output = new FileOutputStream(destino);

                byte data[] = new byte[1024];
                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // Calcula a porcentagem atual e envia para a tela do celular
                    publishProgress((int) ((total * 100) / tamanhoArquivo));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

            } catch (Exception e) {
                return e.getMessage();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Atualiza a barra azul de porcentagem na tela do usuário
            progressBar.setProgress(progress[0]);
            txtStatus.setText("Baixando " + nomeSistema + "... (" + progress[0] + "%)");
        }

        @Override
        protected void onPostExecute(String result) {
            layoutProgress.setVisibility(View.GONE);
            if (result == null) {
                txtStatus.setText(nomeSistema + " pronto para rodar no Qemulator!");
            }
        }
    }
}
