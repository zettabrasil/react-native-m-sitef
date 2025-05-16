package br.com.zettabrasil.msitef;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import android.content.Intent;
import android.app.Activity;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.com.execucao.posmp_api.SmartPosHelper;
import br.com.execucao.posmp_api.printer.PrinterService;
import br.com.execucao.posmp_api.store.AppStatus;
import br.com.execucao.smartPOSService.printer.IOnPrintFinished;

import static android.app.Activity.RESULT_OK;
import static android.app.Activity.RESULT_CANCELED;

public class MSitefModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private static ReactApplicationContext reactContext;
    private static final int SITEF_REQUEST_CODE = 4321;
    private PrinterService printerService;

    MSitefModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
        initializeSmartPosHelper(context);
        initializePrinterService();
        context.addActivityEventListener(this);
    }

    private void sendEvent(ReactContext context, String name, @Nullable WritableMap params) {
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(name, params);
    }

    /**
     * Inicialização da biblioteca auxiliar SmartPOS
     */
    private void initializeSmartPosHelper(ReactApplicationContext context) {
        if (SmartPosHelper.getInstance() == null) {
            SmartPosHelper.init(context, AppStatus.ACTIVE);
        }
    }

    /**
     * Inicializa o serviço da impressora
     */
    private void initializePrinterService() {
        printerService = SmartPosHelper.getInstance().getPrinter();
        printerService.open();

    }

    /**
     * Verifica se o serviço de impressora está disponível.
     */
    private boolean isPrinterServiceAvailable() {
        return printerService != null;
    }

    private Intent getDefaultIntent(ReadableMap data) {
        Intent i = new Intent("br.com.softwareexpress.sitef.msitef.ACTIVITY_CLISITEF");
        String modalidade = data.getString("modalidade");

        if (modalidade == null) {
            modalidade = "0";
        }

        i.putExtra("modalidade", modalidade);

        if (modalidade.equals("119")) {
            i.putExtra("tokenRegistroTls", data.getString("tokenRegistroTls"));
        }

        String currentDate = data.getString("data");
        String currentTime = data.getString("hora");
        String terminalSitef = data.getString("terminalSitef");

        if (terminalSitef != null) {
            i.putExtra("terminalSitef", terminalSitef);
        }

        i.putExtra("empresaSitef", data.getString("empresaSitef"));
        i.putExtra("enderecoSitef", data.getString("enderecoSitef"));
        i.putExtra("operador", data.getString("operador"));
        i.putExtra("data", currentDate != null ? currentDate : getCurrentDate());
        i.putExtra("hora", currentTime != null ? currentTime : getCurrentTime());
        i.putExtra("numeroCupom", data.getString("numeroCupom"));
        i.putExtra("numParcelas", data.getString("numParcelas"));
        i.putExtra("valor", data.getString("valor"));
        i.putExtra("restricoes", data.getString("restricoes"));
        i.putExtra("acessibilidadeVisual", data.getString("acessibilidadeVisual"));
        i.putExtra("comExterna", data.getString("comExterna"));
        i.putExtra("CNPJ_CPF", data.getString("cnpjCpf"));
        i.putExtra("cnpj_automacao", data.getString("cnpjAutomacao"));
        i.putExtra("cnpj_facilitador", data.getString("cnpjFacilitador"));
        i.putExtra("timeoutColeta", data.getString("timeoutColeta"));
        i.putExtra("transacoesHabilitadas", data.getString("transacoesHabilitadas"));

        return i;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == SITEF_REQUEST_CODE) {
            WritableMap params = Arguments.createMap();

            if (resultCode == RESULT_OK) {
                params.putString("type", "finished");
                params.putString("message", "Transação Concluída");

                WritableMap resultData = Arguments.createMap();

                if (data != null && data.getExtras() != null) {
                    resultData.putString("codigoResposta", data.getExtras().getString("CODRESP"));
                    resultData.putString("dataConfirmacao", data.getExtras().getString("COMP_DADOS_CONF"));
                    resultData.putString("codigoTransacao", data.getExtras().getString("CODTRANS"));
                    resultData.putString("tipoParcelamento", data.getExtras().getString("TIPO_PARC"));
                    resultData.putString("valorTroco", data.getExtras().getString("VLTROCO"));
                    resultData.putString("redeAutorizadora", data.getExtras().getString("REDE_AUT"));
                    resultData.putString("bandeira", data.getExtras().getString("BANDEIRA"));
                    resultData.putString("nsuSitef", data.getExtras().getString("NSU_SITEF"));
                    resultData.putString("nsuHost", data.getExtras().getString("NSU_HOST"));
                    resultData.putString("codigoAutorizacao", data.getExtras().getString("COD_AUTORIZACAO"));
                    resultData.putString("numeroParcelas", data.getExtras().getString("NUM_PARC"));
                    resultData.putString("viaEstabelecimento", data.getExtras().getString("VIA_ESTABELECIMENTO"));
                    resultData.putString("viaCliente", data.getExtras().getString("VIA_CLIENTE"));
                }

                params.putMap("data", resultData);
            } else if (resultCode == RESULT_CANCELED) {
                params.putString("type", "canceled");
                params.putString("message", "Transação Cancelada");
            } else {
                params.putString("type", "error");
                params.putString("message", "Erro na transação");
            }

            sendEvent(reactContext, "events", params);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // Não necessário para este caso
    }

    @NonNull
    @Override
    public String getName() {
        return "MSitef";
    }

    @ReactMethod
    public void launch(ReadableMap data) {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            Log.e("MSitefModule", "Nenhuma atividade disponível para iniciar a transação");
            WritableMap params = Arguments.createMap();
            params.putString("type", "error");
            params.putString("message", "Nenhuma atividade disponível para iniciar a transação");
            sendEvent(reactContext, "events", params);
            return;
        }

        Intent i = getDefaultIntent(data);
        try {
            currentActivity.startActivityForResult(i, SITEF_REQUEST_CODE);
        } catch (Exception e) {
            Log.e("MSitefModule", "Erro ao iniciar a transação: " + e.getMessage());
            WritableMap params = Arguments.createMap();
            params.putString("type", "error");
            params.putString("message", "Erro ao iniciar a transação: " + e.getMessage());
            sendEvent(reactContext, "events", params);
        }
    }

    /**
     * Imprime o comprovante como texto, alinhado à direita.
     */
    @ReactMethod
    public void printReceipt(String receipt) {
        WritableMap params = Arguments.createMap();

        if (!isPrinterServiceAvailable()) {
            params.putString("type", "error");
            params.putString("message", "Lib de Impressão: Impressora indisponível");
            sendEvent(reactContext, "events", params);
            return;
        }

        if (isPrinterServiceAvailable()) {

            String formattedReceipt = receipt.replace(": ", ":")
                    .replace(" T", "T")
                    .replace(" R", "R")
                    .replace(" F", "F");


            printerService.printText(formattedReceipt,
                    new IOnPrintFinished.Stub() {
                        @Override
                        public void onSuccess() {
                            params.putString("type", "success");
                            params.putString("message", "Lib de Impressão: Impressão concluída");
                            sendEvent(reactContext, "events", params);
                        }

                        @Override
                        public void onFailed(int error, String msg) {
                            params.putString("type", "error");
                            params.putString("message", "Lib de Impressão: " + msg);
                            sendEvent(reactContext, "events", params);
                        }
                    }
            );
        }
    };

    public static String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getCurrentTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss", Locale.getDefault());
        Date date = new Date();
        return timeFormat.format(date);
    }
}

