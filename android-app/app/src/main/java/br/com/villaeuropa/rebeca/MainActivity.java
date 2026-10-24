package br.com.villaeuropa.rebeca;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final String HOME = "https://khassiooluver1988-droid.github.io/Sara-Ia-23-10-2026/";
    private static final int AUDIO_PERMISSION_CODE = 431;
    private WebView webView;
    private SpeechRecognizer recognizer;
    private PermissionRequest pendingWebPermission;
    private boolean startAfterPermission;

    private boolean isTrusted(Uri uri) {
        return uri != null && "https".equalsIgnoreCase(uri.getScheme())
            && "khassiooluver1988-droid.github.io".equalsIgnoreCase(uri.getHost())
            && uri.getPath() != null && uri.getPath().startsWith("/Sara-Ia-23-10-2026/");
    }

    private void js(String code) {
        if (webView != null) webView.evaluateJavascript(code, null);
    }

    private void showVoiceStatus(String message) {
        js("(function(){var el=document.getElementById('voiceStatus');if(el)el.textContent="
            + org.json.JSONObject.quote(message) + ";})()");
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(18,59,97));
        getWindow().setNavigationBarColor(Color.rgb(18,59,97));
        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webView.getSettings().setSupportMultipleWindows(false);
        webView.addJavascriptInterface(new VoiceBridge(), "RebecaAndroid");
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (isTrusted(uri)) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
                catch (Exception ignored) { }
                return true;
            }
            @Override public void onPageFinished(WebView view, String url) {
                if (!isTrusted(Uri.parse(url))) return;
                js("(function(){var mic=document.getElementById('mic');"
                    + "if(!mic||!window.RebecaAndroid)return;"
                    + "mic.onclick=function(e){e.preventDefault();window.RebecaAndroid.startListening();};"
                    + "})()");
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> {
                    if (!isTrusted(request.getOrigin())) { request.deny(); return; }
                    boolean audio = false;
                    for (String resource : request.getResources())
                        if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource)) audio = true;
                    if (!audio) { request.deny(); return; }
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        request.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
                    } else {
                        pendingWebPermission = request;
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_CODE);
                    }
                });
            }
        });
        setContentView(webView);
        webView.loadUrl(HOME);
    }

    public class VoiceBridge {
        @JavascriptInterface public void startListening() {
            runOnUiThread(() -> startNativeRecognition());
        }
    }

    private void startNativeRecognition() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            startAfterPermission = true;
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_CODE);
            return;
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            showVoiceStatus("Reconhecimento por voz indisponível neste aparelho. Digite sua pergunta.");
            return;
        }
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.destroy();
        }
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle b) {
                showVoiceStatus("Microfone ativo. Fale sua pergunta.");
                js("document.getElementById('mic').setAttribute('aria-pressed','true')");
            }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rms) {}
            @Override public void onBufferReceived(byte[] bytes) {}
            @Override public void onEndOfSpeech() {
                showVoiceStatus("Processando sua pergunta por voz…");
            }
            @Override public void onError(int error) {
                showVoiceStatus("Não foi possível reconhecer a fala. Tente novamente ou digite.");
                js("document.getElementById('mic').setAttribute('aria-pressed','false')");
            }
            @Override public void onResults(Bundle results) {
                ArrayList<String> phrases = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                js("document.getElementById('mic').setAttribute('aria-pressed','false')");
                if (phrases == null || phrases.isEmpty()) {
                    showVoiceStatus("Não consegui entender. Tente novamente.");
                    return;
                }
                String phrase = phrases.get(0);
                js("(function(){var field=document.getElementById('msg');var send=document.getElementById('send');"
                    + "if(field&&send){field.value=" + org.json.JSONObject.quote(phrase)
                    + ";field.dispatchEvent(new Event('input',{bubbles:true}));var voice=document.getElementById('voiceToggle');if(voice&&voice.getAttribute('aria-pressed')!=='true')voice.click();send.click();}})()");
                showVoiceStatus("Pergunta transcrita e enviada à Rebeca.");
            }
            @Override public void onPartialResults(Bundle results) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        recognizer.startListening(intent);
    }

    @Override public void onRequestPermissionsResult(int code, String[] permissions, int[] grants) {
        super.onRequestPermissionsResult(code, permissions, grants);
        if (code != AUDIO_PERMISSION_CODE) return;
        boolean allowed = grants.length > 0 && grants[0] == PackageManager.PERMISSION_GRANTED;
        if (pendingWebPermission != null) {
            if (allowed) pendingWebPermission.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
            else pendingWebPermission.deny();
            pendingWebPermission = null;
        }
        if (startAfterPermission) {
            startAfterPermission = false;
            if (allowed) startNativeRecognition();
            else showVoiceStatus("Permissão do microfone não concedida.");
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (recognizer != null) { recognizer.cancel(); recognizer.destroy(); recognizer = null; }
        if (webView != null) { webView.destroy(); webView = null; }
        super.onDestroy();
    }
}
