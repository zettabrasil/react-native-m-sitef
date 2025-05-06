package br.com.zettabrasil.msitef;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.List;

public class MSitefModule extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;

    MSitefModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    private void sendEvent(ReactContext reactContext, String name, @Nullable WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(name, params);
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

    private void handleActivityResult(ActivityResult result) {
        WritableMap params = Arguments.createMap();

        if (result.getResultCode() == RESULT_OK) {
            params.putString("type", "finished");
            params.putString("message", "Transação Concluída");

            WritableMap data = Arguments.createMap();
            Intent res = result.getData();

            data.putString("codigoResposta", res.getExtras().getString("CODRESP"));
            data.putString("dataConfirmacao", res.getExtras().getString("COMP_DADOS_CONF"));
            data.putString("codigoTransacao", res.getExtras().getString("CODTRANS"));
            data.putString("tipoParcelamento", res.getExtras().getString("TIPO_PARC"));
            data.putString("valorTroco", res.getExtras().getString("VLTROCO"));
            data.putString("redeAutorizadora", res.getExtras().getString("REDE_AUT"));
            data.putString("bandeira", res.getExtras().getString("BANDEIRA"));
            data.putString("nsuSitef", res.getExtras().getString("NSU_SITEF"));
            data.putString("nsuHost", res.getExtras().getString("NSU_HOST"));
            data.putString("codigoAutorizacao", res.getExtras().getString("COD_AUTORIZACAO"));
            data.putString("numeroParcelas", res.getExtras().getString("NUM_PARC"));
            data.putString("viaEstabelecimento", res.getExtras().getString("VIA_ESTABELECIMENTO"));
            data.putString("viaCliente", res.getExtras().getString("VIA_CLIENTE"));

            params.putMap("data", data);
        } else if (result.getResultCode() == RESULT_CANCELED) {
            params.putString("type", "canceled");
            params.putString("message", "Transação Cancelada");
        } else {
            params.putString("type", "error");
            params.putString("message", "Erro na transação");
        }

        sendEvent(reactContext, "events", params);
    }

    @NonNull
    @Override
    public String getName() {
        return "MSitef";
    }

    @ReactMethod
    public void launch(ReadableMap data) {
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), o -> {
                    handleActivityResult(o);
                }
        );
        Intent i = getDefaultIntent(data);
        activityResultLauncher.launch(i);
    }

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

