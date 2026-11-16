package com.rockydrive.auth.repository;

import com.rockydrive.auth.model.Device;
import com.rockydrive.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

    Optional<Device> findByUserAndDeviceId(User user, String deviceId);
}
