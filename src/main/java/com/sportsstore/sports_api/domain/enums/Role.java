package com.sportsstore.sports_api.domain.enums;

//En Spring Security, por convención, los roles suelen llevar
//el prefijo ROLE_ cuando se validan, pero en la base de datos
//podemos guardarlos limpios.
public enum Role {
    CUSTOMER,
    ADMIN
}
