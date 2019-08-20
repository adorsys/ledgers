package de.adorsys.ledgers.middleware.api.service;


import de.adorsys.ledgers.middleware.api.domain.um.ScaMethodTypeTO;

public interface ScaChallengeDataResolver {
    ScaChallengeData resolveScaChallengeData(ScaMethodTypeTO scaMethodTypeTO);
}
