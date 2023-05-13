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

package org.cxbox.testing.conf;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cxbox.core.dao.BaseDAO;
import org.cxbox.model.core.config.ScopeConfig;
import org.cxbox.model.core.listeners.jpa.DelegatingBaseEntityListener;
import org.cxbox.model.core.listeners.jpa.ExtRevisionListener;
import org.cxbox.model.core.service.CxboxBaseEntityListenerDelegate;
import org.cxbox.model.core.tx.TransactionServiceImpl;
import org.cxbox.model.core.tx.TransactionStatusImpl;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;


@Import({
		ScopeConfig.class,
		TransactionServiceImpl.class,
		TransactionStatusImpl.class,
		ExtRevisionListener.class,
		DelegatingBaseEntityListener.class,
		CxboxBaseEntityListenerDelegate.class
})
public class PersistenceTestContext {

	@Bean
	public BaseDAO baseDAO() {
		return mock(BaseDAO.class);
	}

	@Bean("primaryDS")
	public DataSource primaryDS() throws SQLException {
		Connection connection = mock(Connection.class);
		Statement statement = mock(Statement.class);
		when(connection.createStatement()).thenReturn(statement);
		DataSource dataSource = mock(DataSource.class);
		when(dataSource.getConnection()).thenReturn(connection);
		when(dataSource.getConnection(any(), any())).thenReturn(connection);
		return dataSource;
	}

}
