package org.cxbox.meta.additionalFields;/*
 * © OOO "SI IKS LAB", 2022-2023
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.core.util.session.SessionService;
import org.cxbox.meta.additionalFields.AdditionalFieldsService;
import org.cxbox.meta.entity.AdditionalFields;
import org.cxbox.meta.entity.AdditionalFields_;
import org.cxbox.model.core.dao.JpaDao;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AdditionalFieldsImpl implements AdditionalFieldsService {

	private final JpaDao jpaDao;

	private final SessionService service;

	private final ObjectMapper objectMapper;

	private final TransactionService transactionService;

	@Override
	public List<AdditionalFieldsDTO> createAdditionalFields(List<AdditionalFieldsDTO> additionalFieldsDTO) {
		List<AdditionalFieldsDTO> additionalFieldsList = new ArrayList<>();
		return transactionService.invokeInTx(() -> {

			additionalFieldsDTO
					.stream().map(this::additionalFieldsFromDTO)
					.forEach(af -> {
						af.setUserId(String.valueOf(service.getSessionUser().getId()));
						if (isExists(af)) {
							updateAdditionalFields(af);
						} else {
							jpaDao.save(af);
						}
						additionalFieldsList.add(new AdditionalFieldsDTO(af));
					});
			return additionalFieldsList;
		});
	}

	@Override
	public void deleteAdditionalFields(List<Long> ids) {
		transactionService.invokeInTx(() -> {
			ids.forEach(id -> {
				jpaDao.delete(AdditionalFields.class, id);
			});
			return null;
		});
	}

	@Override
	public List<AdditionalFieldsDTO> updateAdditionalFields(List<AdditionalFieldsDTO> additionalFieldsDTO) {
		List<AdditionalFieldsDTO> additionalFieldsDTOList = new ArrayList<>();
		transactionService.invokeInTx(() -> {
			additionalFieldsDTO
					.stream().map(this::additionalFieldsFromDTO)
					.forEach(af -> {
						af.setUserId(String.valueOf(service.getSessionUser().getId()));
						if (isExists(af)) {
							updateAdditionalFields(af);
							additionalFieldsDTOList.add(new AdditionalFieldsDTO(af));
						}
					});
			return null;
		});
		return additionalFieldsDTOList;
	}

	private boolean isExists(AdditionalFields additionalFields) {
		return jpaDao.exists(
				AdditionalFields.class,
				(root, cq, cb) -> cb.and(
						cb.equal(root.get(AdditionalFields_.view), additionalFields.getView()),
						cb.equal(root.get(AdditionalFields_.widget), additionalFields.getWidget()),
						cb.equal(
								root.get(AdditionalFields_.userId),
								String.valueOf(service.getSessionUser().getId())
						)
				)
		);
	}

	private void updateAdditionalFields(AdditionalFields additionalFieldsForUpdate) {
		jpaDao.update(
				AdditionalFields.class, (root, cq, cb) -> cb.and(
						cb.equal(root.get(AdditionalFields_.view), additionalFieldsForUpdate.getView()),
						cb.equal(root.get(AdditionalFields_.widget), additionalFieldsForUpdate.getWidget()),
						cb.equal(root.get(AdditionalFields_.userId), String.valueOf(service.getSessionUser().getId()))
				),
				(update, root, cb) -> {
					update.set(
							root.get(AdditionalFields_.orderFields),
							cb.literal(Optional.ofNullable(additionalFieldsForUpdate.getOrderFields()).orElse("[]"))
					);
					update.set(
							root.get(AdditionalFields_.addedToAdditionalFields),
							cb.literal(Optional.ofNullable(additionalFieldsForUpdate.getAddedToAdditionalFields()).orElse("[]"))
					);
					update.set(
							root.get(AdditionalFields_.removedFromAdditionalFields),
							cb.literal(Optional.ofNullable(additionalFieldsForUpdate.getRemovedFromAdditionalFields()).orElse("[]"))
					);
				}
		);
	}

	@SneakyThrows
	private AdditionalFields additionalFieldsFromDTO(AdditionalFieldsDTO additionalFieldsDTO) {
		return new AdditionalFields()
				.setAddedToAdditionalFields(objectMapper
						.writeValueAsString(additionalFieldsDTO.getAddedToAdditionalFields()))
				.setRemovedFromAdditionalFields(objectMapper
						.writeValueAsString(additionalFieldsDTO.getOrderFields()))
				.setOrderFields(objectMapper
						.writeValueAsString(additionalFieldsDTO.getOrderFields()))
				.setWidget(additionalFieldsDTO.getWidget())
				.setView(additionalFieldsDTO.getView());
	}

}
