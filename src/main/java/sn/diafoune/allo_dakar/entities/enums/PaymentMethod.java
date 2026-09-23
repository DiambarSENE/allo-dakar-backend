package sn.diafoune.allo_dakar.entities.enums;

/**
 * NB : la méthode TOKEN du cahier des charges initial a été retirée à la demande du client
 * (système de jetons/crédits hors périmètre — voir §17 exclu).
 */
public enum PaymentMethod {
    CASH,
    CARD,
    MOBILE_MONEY,
    BANK_TRANSFER,
    OTHER
}
