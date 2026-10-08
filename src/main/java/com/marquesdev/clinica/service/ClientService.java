package com.marquesdev.clinica.service;

import com.marquesdev.clinica.dto.ClientRequestDto;
import com.marquesdev.clinica.dto.ClientResponseDto;
import com.marquesdev.clinica.dto.ClientUpdateRequestDto;
import com.marquesdev.clinica.entity.Client;
import com.marquesdev.clinica.entity.User;
import com.marquesdev.clinica.exception.EntityNotFoundException;
import com.marquesdev.clinica.integration.viacep.ViaCepResponseDto;
import com.marquesdev.clinica.integration.viacep.ViaCepService;
import com.marquesdev.clinica.mapper.ClientMapper;
import com.marquesdev.clinica.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final UserService userService;
    private final ViaCepService viaCepService;

    public ClientResponseDto createClient(String email, ClientRequestDto clientRequestDto) {
        User user = userService.findByEmail(email);
        Client client = clientMapper.toClient(clientRequestDto);

        client.assignUser(user);

        ViaCepResponseDto address = viaCepService.findAddressByCep(clientRequestDto.cep());

        updateAddress(client, address);

        Client savedClient = clientRepository.save(client);

        return clientMapper.toDto(savedClient);
    }


    public ClientResponseDto getMyClient(String email) {
        Client client = clientRepository.findByUserEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));
        return clientMapper.toDto(client);
    }


    public ClientResponseDto updateClient(
            String email,
            ClientUpdateRequestDto clientUpdateRequestDto
    ) {
        Client client = clientRepository.findByUserEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));
        clientMapper.updateClient(clientUpdateRequestDto, client);

        if (clientUpdateRequestDto.cep() != null) {
            ViaCepResponseDto address = viaCepService.findAddressByCep(clientUpdateRequestDto.cep());
            updateAddress(client, address);
        }
        Client updatedClient = clientRepository.save(client);
        return clientMapper.toDto(updatedClient);
    }

    private void updateAddress(
            Client client,
            ViaCepResponseDto address
    ) {
        client.setStreet(address.logradouro());
        client.setNeighborhood(address.bairro());
        client.setCity(address.localidade());
        client.setState(address.uf());
    }
}
