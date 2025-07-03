/*
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

package org.cxbox.core.dto.rowmeta;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.NonNull;
import org.cxbox.api.data.BcIdentifier;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.constgen.DtoField;
import org.cxbox.core.dto.DrillDownType;
import org.cxbox.core.dto.MessageType;
import org.cxbox.core.service.action.DrillDownTypeSpecifier;
import org.cxbox.core.util.SpringBeanUtils;
import org.cxbox.core.service.drilldown.PlatformDrilldownService;
import org.cxbox.core.service.drilldown.filter.FC;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostAction {

	private final Map<String, Object> attributes = new HashMap<>();

	public static PostAction refreshBc(BcIdentifier bcIdentifier) {
		return new PostAction()
				.add(BasePostActionField.TYPE, BasePostActionType.REFRESH_BC)
				.add(BasePostActionField.BC, bcIdentifier.getName());
	}

	public static PostAction refreshParentBc(BcIdentifier bcIdentifier) {
		return new PostAction()
				.add(BasePostActionField.TYPE, BasePostActionType.REFRESH_BC)
				.add(BasePostActionField.BC, bcIdentifier.getParentName());
	}

	public static PostAction downloadFile(String fileId) {
		return new PostAction()
				.add(BasePostActionField.TYPE, BasePostActionType.DOWNLOAD_FILE)
				.add(BasePostActionField.FILE_ID, fileId);
	}

	public static PostAction downloadFileByUrl(String url) {
		return new PostAction()
				.add(BasePostActionField.TYPE, BasePostActionType.DOWNLOAD_FILE_BY_URL)
				.add(BasePostActionField.URL, url);
	}

	public static PostAction openPickList(final String pickList) {
		return new PostAction()
				.add(BasePostActionField.TYPE, BasePostActionType.OPEN_PICK_LIST)
				.add(BasePostActionField.PICK_LIST, pickList);
	}

	public static PostAction drillDown(DrillDownTypeSpecifier drillDownType, String url) {
		return drillDown(drillDownType, url, null);
	}

	public static PostAction drillDown(DrillDownTypeSpecifier drillDownType, String url, String urlName) {
		return new PostAction()
				.add(BasePostActionField.TYPE, BasePostActionType.DRILL_DOWN)
				.add(BasePostActionField.URL, url)
				.add(BasePostActionField.URL_NAME, urlName)
				.add(BasePostActionField.DRILL_DOWN_TYPE, drillDownType.getValue());
	}

	/**
	 * Sets drill-down functionality with filter capabilities for a specific field.
	 *<p>
	 * This method configures a drill-down URL with optional filtering parameters for a DTO field.
	 * It retrieves the {@link PlatformDrilldownService} to generate URL filter parameters and applies them
	 * to the field's drill-down configuration.
	 *</p>
	 * <pre>{@code
	 * Example:
	 * 			 // add with default builder
	 * 			 PostAction.drillDownWithFilterDrillDownType.INNER, "screen/myscreen/view/myview",
	 * 			 fc -> fc
	 * 			  .add(RestController.myBc, MyDefaultDTO.class, fb -> fb
	 *					.dictionaryEnum(MyDefaultDTO_.status, getStatusFilterValues(id))
	 *					.multiValue(MyDefaultDTO_.multivalueField, myMultivalueField))
	 *				 // add with custom filter builders
	 *				.add(RestController.myBc, MyDefaultDTO.class,
	 *				  new TypeToken<MyCustomFilterBuilder<MyCustomDTO>>() {
	 *
	 *				  },
	 *				  fb -> fb
	 *				   .dictionaryEnum(MyDTO_.status, getStatusFilterValues(id))
	 *				   .multiValue(MyDTO_.multivalueField, myMultivalueFilterField)
	 *				   .myCustomFields(MyDTO_.customField, myCustomFieldFilterValue
	 * 		);
	 * }</pre>
	 * @param drillDownType the type specifier that defines the drill-down behavior
	 * @param url the base drill-down URL string
	 * @param fc a consumer that accepts and configures the filter configuration object.
	 *                   This allows customization of filtering parameters that will be appended
	 *                   to the drill-down URL
	 */
	public static PostAction drillDownWithFilter(DrillDownTypeSpecifier drillDownType,
			String url,
			Consumer<FC> fc) {
		FC fcInstance = new FC();
		fc.accept(fcInstance);
		var platformDrilldownService = SpringBeanUtils.getBean(PlatformDrilldownService.class);
		return new PostAction()
				.add(BasePostActionField.TYPE, BasePostActionType.DRILL_DOWN)
				.add(
						BasePostActionField.URL,
						url + Optional.ofNullable(platformDrilldownService.formUrlFilterPart(fcInstance)).map(fp -> "?" + fp).orElse("")
				)
				.add(BasePostActionField.URL_NAME, null)
				.add(BasePostActionField.DRILL_DOWN_TYPE, drillDownType.getValue());
	}


	public static PostAction delayedRefreshBC(BcIdentifier bcIdentifier, Number seconds) {
		return new PostAction()
				.add(BasePostActionField.TYPE, BasePostActionType.DELAYED_REFRESH_BC)
				.add(BasePostActionField.BC, bcIdentifier.getName())
				.add(BasePostActionField.DELAY, seconds.toString());
	}

	public static PostAction showMessage(MessageType messageType, String messageText) {
		return new PostAction()
				.add(BasePostActionField.TYPE, BasePostActionType.SHOW_MESSAGE)
				.add(BasePostActionField.MESSAGE_TYPE, messageType.getValue())
				.add(BasePostActionField.MESSAGE_TEXT, messageText);
	}

	public static PostAction postDelete() {
		return new PostAction()
				.add(BasePostActionField.TYPE, BasePostActionType.POST_DELETE);
	}

	/**
	 * Creates a post-action that waits until a specified condition is met.
	 *
	 * @param successConditionField The field whose value will be monitored.
	 * @param successConditionValue The value that indicates successful completion. Only String and Boolean types are supported now
	 * @return A builder for constructing a `waitUntil` post-action.
	 *
	 * <p><b>Example usage:</b>
	 * <pre>
	 * PostAction.waitUntil(CxboxRestController.clientEdit, ClientDTO_.statusField, true)
	 *          .inProgressMessage("Fetching data...")
	 *          .successMessage("Data received. Click OK to proceed.")
	 *          .timeoutMessage("Timeout reached. Close the window and refresh.")
	 *          .build();
	 * </pre>
	 */
	public static <T extends DataResponseDTO, V> WaitUntilBuilder<T, V> waitUntil(
			@NonNull DtoField<? super T, V> successConditionField, @NonNull V successConditionValue) {
		return new WaitUntilBuilder<>(successConditionField, successConditionValue);
	}

	/**
	 * Creates a post-action that performs a INNER  drill-down and then waits until a specified condition is met.
	 *
	 * @param url The INNER drill-down URL to navigate to.
	 * @param successConditionBc The business component on which the condition will be monitored after drill-down.
	 * @param successConditionField The field whose value will be monitored.
	 * @param successConditionValue The value that indicates successful completion. Only String and Boolean types are supported now
	 * @return A builder for constructing a `drillDownAndWaitUntil` post-action.
	 *
	 * <p><b>Example usage:</b>
	 * <pre>
	 * PostAction.drillDownAndWaitUntil("/screen/client/view/clientView/" + CxboxRestController.clientEdit + "/" + clientId,
	 *           CxboxRestController.clientEdit, ClientDTO_.statusField, true)
	 *          .inProgressMessage("Navigating and fetching data...")
	 *          .successMessage("Data loaded successfully.")
	 *          .timeoutMessage("Failed to retrieve data in time.")
	 *          .build();
	 * </pre>
	 */
	public static <T extends DataResponseDTO, V> WaitUntilBuilder<T, V> drillDownAndWaitUntil(@NonNull String url,
			@NonNull BcIdentifier successConditionBc, @NonNull DtoField<? super T, V> successConditionField, @NonNull V successConditionValue) {
		return new WaitUntilBuilder<>(
				DrillDownType.INNER,
				url,
				successConditionBc,
				successConditionField,
				successConditionValue
		);
	}

	@JsonAnyGetter
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@JsonAnyGetter
	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	public String getURL() {
		return (String) attributes.get(BasePostActionField.URL);
	}

	public String getType() {
		return (String) attributes.get(BasePostActionField.TYPE);
	}

	public String getDrillDownType() {
		return (String) attributes.get(BasePostActionField.DRILL_DOWN_TYPE);
	}

	public PostAction add(String key, Object value) {
		attributes.put(key, value);
		return this;
	}


	/**
	 * Creates a post-action that waits until a specified condition is met.
	 * <br>
	 * <br>
	 * Must be created only from {@link PostAction#waitUntil(DtoField, Object)} or {@link PostAction#drillDownAndWaitUntil(String, BcIdentifier, DtoField, Object)})
	 **/
	public static class WaitUntilBuilder<T extends DataResponseDTO, V> {

		private final String type;

		private final DrillDownTypeSpecifier drillDownType;

		private final String url;

		private final String successConditionBc;

		private final DtoField<? super T, V> successConditionField;

		private final V successConditionValue;

		private String inProgressMessage;

		private String successMessage;

		private Duration timeout = Duration.ofSeconds(5);

		private int timeoutMaxRequests = 3;

		private String timeoutMessage;

		private WaitUntilBuilder(@NonNull DrillDownTypeSpecifier drillDownType, @NonNull String url,
				@NonNull BcIdentifier successConditionBc, @NonNull DtoField<? super T, V> successConditionField,
				@NonNull V successConditionValue) {
			this.type = BasePostActionType.DRILL_DOWN_AND_WAIT_UNTIL;
			this.drillDownType = drillDownType;
			this.url = url;
			this.successConditionBc = successConditionBc.getName();
			this.successConditionField = successConditionField;
			this.successConditionValue = successConditionValue;
		}

		private WaitUntilBuilder(@NonNull DtoField<? super T, V> successConditionField, @NonNull V successConditionValue) {
			this.type = BasePostActionType.WAIT_UNTIL;
			this.drillDownType = null;
			this.url = null;
			this.successConditionBc = null;
			this.successConditionField = successConditionField;
			this.successConditionValue = successConditionValue;
		}

		/**
		 * Sets the message displayed while waiting for the operation to complete. If not set - default configured in UI is displayed
		 *
		 * @param message The message to display.
		 * @return The builder instance.
		 */
		public WaitUntilBuilder<T, V> inProgressMessage(@NonNull String message) {
			this.inProgressMessage = message;
			return this;
		}

		/**
		 * Sets the message to displayed when operation successfully completes before timeout and forces UI to wait user to close popup manually. If not set - wait popup will be autoclosed immediately
		 * @param message message displayed when operation complete before timeout (e.g. successConditionField achieved successConditionValue)
		 * @return The builder instance.
		 */
		public WaitUntilBuilder<T, V> successMessage(@NonNull String message) {
			this.successMessage = message;
			return this;
		}

		/**
		 * timeout (default and recommended value is 5 seconds).
		 */
		public WaitUntilBuilder<T, V> timeout(@NonNull Duration timeout) {
			this.timeout = timeout;
			return this;
		}

		/**
		 * number of intermediate requests to check if successConditionField achieved successConditionValue before timeout is reached
		 */
		public WaitUntilBuilder<T, V> timeoutMaxRequests(int maxRequests) {
			this.timeoutMaxRequests = maxRequests;
			return this;
		}

		/**
		 * Sets the message to displayed when operation completes because of timeout and forces UI to wait user to close popup manually. If not set - wait popup will be autoclosed immediately
		 * @param message The message to display if operation completed because timeout reached
		 * @return The builder instance.
		 */
		public WaitUntilBuilder<T, V> timeoutMessage(@NonNull String message) {
			this.timeoutMessage = message;
			return this;
		}

		public PostAction build() {
			PostAction postAction = new PostAction()
					.add(BasePostActionField.TYPE, type)
					.add(BasePostActionField.SUCCESS_CONDITION_BC, successConditionBc)
					.add(BasePostActionField.SUCCESS_CONDITION_FIELD, successConditionField.getName())
					.add(BasePostActionField.SUCCESS_CONDITION_VALUE, successConditionValue)
					.add(BasePostActionField.IN_PROGRESS_MESSAGE, inProgressMessage)
					.add(BasePostActionField.SUCCESS_MESSAGE, successMessage)
					.add(BasePostActionField.TIMEOUT, String.valueOf(timeout.toMillis()))
					.add(BasePostActionField.TIMEOUT_MAX_REQUESTS, String.valueOf(timeoutMaxRequests))
					.add(BasePostActionField.TIMEOUT_MESSAGE, timeoutMessage);

			if (drillDownType != null) {
				postAction.add(BasePostActionField.DRILL_DOWN_TYPE, drillDownType.getValue());
				postAction.add(BasePostActionField.URL, url);
			}

			return postAction;
		}

	}

	public static class BasePostActionType {

		private BasePostActionType() {
		}

		public static final String REFRESH_BC = "refreshBC";

		public static final String DOWNLOAD_FILE = "downloadFile";

		public static final String DOWNLOAD_FILE_BY_URL = "downloadFileByUrl";

		public static final String OPEN_PICK_LIST = "openPickList";

		public static final String DRILL_DOWN = "drillDown";

		public static final String DELAYED_REFRESH_BC = "delayedRefreshBC";

		public static final String SHOW_MESSAGE = "showMessage";

		public static final String POST_DELETE = "postDelete";

		public static final String WAIT_UNTIL = "waitUntil";

		public static final String DRILL_DOWN_AND_WAIT_UNTIL = "drillDownAndWaitUntil";

	}

	public static class BasePostActionField {

		private BasePostActionField() {
		}

		public static final String TYPE = "type";

		public static final String BC = "bc";

		public static final String FILE_ID = "fileId";

		public static final String DELAY = "delay";

		public static final String MESSAGE_TYPE = "messageType";

		public static final String MESSAGE_TEXT = "messageText";

		public static final String URL = "url";

		public static final String URL_NAME = "urlName";

		public static final String DRILL_DOWN_TYPE = "drillDownType";

		public static final String PICK_LIST = "pickList";

		public static final String SUCCESS_CONDITION_BC = "successCondition_bcName";

		public static final String SUCCESS_CONDITION_FIELD = "successCondition_fieldKey";

		public static final String SUCCESS_CONDITION_VALUE = "successCondition_value";

		public static final String SUCCESS_MESSAGE = "successMessage";

		public static final String IN_PROGRESS_MESSAGE = "inProgressMessage";

		public static final String TIMEOUT = "timeout";

		public static final String TIMEOUT_MAX_REQUESTS = "timeoutMaxRequests";

		public static final String TIMEOUT_MESSAGE = "timeoutMessage";

	}

}
