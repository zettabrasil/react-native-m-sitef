import {
  type EmitterSubscription,
  NativeEventEmitter,
  NativeModules,
} from "react-native";

const LINKING_ERROR =
  "This is a exclusive Android lib. Does not work on iOS. \n\n" +
  "The package 'react-native-m-sitef' doesn't seem to be linked. Make sure: \n\n" +
  "- You rebuilt the app after installing the package\n" +
  "- You are not using Expo Go\n";

const MSitefLib = NativeModules.MSitef
  ? NativeModules.MSitef
  : new Proxy(
    {},
    {
      get() {
        throw new Error(LINKING_ERROR);
      },
    },
  );

type EventEmitter = "events";

export type EventTypes =
  | "finished"
  | "canceled"
  | "error";

type EventData = {
  codigoResposta: string;
  dataConfirmacao: string;
  codigoTransacao: string;
  tipoParcelamento: string;
  valorTroco: string;
  redeAutorizadora: string;
  bandeira: string;
  nsuSitef: string;
  nsuHost: string;
  codigoAutorizacao: string;
  numeroParcelas: string;
  viaEstabelecimento: string;
  viaCliente: string;
};

type Handler = {
  type: EventTypes;
  message: string;
  data?: EventData;
};

export type MSitefTransaction = {
  modalidade: string;
  empresaSitef: string;
  enderecoSitef: string;
  cnpjCpf: string;
  numeroCupom: string;
  comExterna: string;
  data: string;
  hora: string;
  valor?: string;
  tokenRegistroTls?: string;
  operador?: string;
  numParcelas?: string;
  cnpjAutomacao?: string;
  cnpjFacilitador?: string;
  restricoes?: string;
  transacoesHabilitadas?: string;
  acessibilidadeVisual?: string;
  timeoutColeta?: string;
  terminalSitef?: string;
}

export type { EmitterSubscription };

export const MSitef = {
  addListener(
    event: EventEmitter,
    handler: (handler: Handler) => void,
  ): EmitterSubscription {
    const eventEmitter = new NativeEventEmitter(MSitefLib);
    return eventEmitter.addListener(event, handler);
  },

  launch(data: MSitefTransaction) {
    MSitefLib.launch(data);
  },
};

export default MSitef;
