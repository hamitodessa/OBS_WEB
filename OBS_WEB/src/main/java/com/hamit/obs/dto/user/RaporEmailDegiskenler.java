package com.hamit.obs.dto.user;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RaporEmailDegiskenler {

	String hesap;
    String isim;
    String too;
    String ccc; 
    String konu;
    String aciklama;
    String nerden;
    String degerler;
    String format ;
    List<Map<String, String>> exceList;
    String tableString;
    String baslik;
}