package com.mensa.zhmensa.services;

import com.mensa.zhmensa.models.Mensa;

import java.util.List;

public interface MensaDownloader {
    public List<Mensa> loadMensaList();
}
