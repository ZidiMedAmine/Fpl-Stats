package com.fpl.stats.services.impl;

import com.fpl.stats.exception.PlayerNotFoundException;
import com.fpl.stats.repository.PlayerRepository;
import com.fpl.stats.services.PlayerService;
import com.fpl.stats.services.dto.PlayerDetailDto;
import com.fpl.stats.services.dto.PlayerSummaryDto;
import com.fpl.stats.services.mapper.PlayerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link PlayerService} for the player comparison feature.
 */
@Service
@Transactional(readOnly = true)
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PlayerSummaryDto> getPlayersByPosition(String position) {
        return playerRepository.findByPosition(position).stream()
                .map(PlayerMapper::toPlayerSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerDetailDto getPlayerDetail(int fplId) {
        return playerRepository.findByFplIdWithHistory(fplId)
                .map(PlayerMapper::toPlayerDetailDto)
                .orElseThrow(() -> new PlayerNotFoundException(fplId));
    }
}
