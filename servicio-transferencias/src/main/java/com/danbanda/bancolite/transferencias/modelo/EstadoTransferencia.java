package com.danbanda.bancolite.transferencias.modelo;

public enum EstadoTransferencia {
    PENDIENTE,   // se registro pero todavia no se mueve el dinero
    COMPLETADA,  // cargo y abono hechos
    RECHAZADA,   // no procede: sin fondos, cuenta no existe, etc.
    REVERTIDA,   // se hizo el cargo, fallo el abono y se le regreso el dinero al origen
    FALLIDA      // algo salio mal y hay que revisarla a mano
}
