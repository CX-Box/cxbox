/*
 * © OOO "SI IKS LAB", 2022-2024
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

package org.cxbox.dictionary.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.IncomparableComparator;
import org.hibernate.type.spi.TypeBootstrapContext;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.UserType;

public abstract class ImmutableType<T> implements UserType<T>, Type, EnhancedUserType<T> {

	private final TypeBootstrapContext configuration;

	private final Class<T> clazz;

	protected ImmutableType(Class<T> clazz) {
		this.clazz = clazz;
		this.configuration = Map::of;
	}

	protected ImmutableType(Class<T> clazz, TypeBootstrapContext configuration) {
		this.clazz = clazz;
		this.configuration = configuration;
	}

	protected TypeBootstrapContext getTypeBootstrapContext() {
		return configuration;
	}

	protected abstract T get(ResultSet rs, int position,
			SharedSessionContractImplementor session, Object owner) throws SQLException;

	protected abstract void set(PreparedStatement st, T value, int index,
			SharedSessionContractImplementor session) throws SQLException;

	/* Methods inherited from the {@link UserType} interface */

	@Override
	public T nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
			throws SQLException {
		return get(rs, position, session, owner);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index,
			SharedSessionContractImplementor session) throws SQLException {
		set(st, clazz.cast(value), index, session);
	}

	@Override
	public Class<T> returnedClass() {
		return clazz;
	}

	@Override
	public boolean equals(Object x, Object y) {
		return (x == y) || (x != null && x.equals(y));
	}

	@Override
	public int hashCode(Object x) {
		return x.hashCode();
	}

	@Override
	public T deepCopy(Object value) {
		return (T) value;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(Object o) {
		return (Serializable) o;
	}

	@Override
	public T assemble(Serializable cached, Object owner) {
		return (T) cached;
	}

	@Override
	public T replace(Object o, Object target, Object owner) {
		return (T) o;
	}

	/* Methods inherited from the {@link Type} interface */

	@Override
	public boolean isAssociationType() {
		return false;
	}

	@Override
	public boolean isCollectionType() {
		return false;
	}

	@Override
	public boolean isEntityType() {
		return false;
	}

	@Override
	public boolean isAnyType() {
		return false;
	}

	@Override
	public boolean isComponentType() {
		return false;
	}

	@Override
	public int getColumnSpan(Mapping mapping) throws MappingException {
		return 1;
	}

	@Override
	public Class<T> getReturnedClass() {
		return returnedClass();
	}

	@Override
	public boolean isSame(Object x, Object y) throws HibernateException {
		return equals(x, y);
	}

	@Override
	public boolean isEqual(Object x, Object y) throws HibernateException {
		return equals(x, y);
	}

	@Override
	public boolean isEqual(Object x, Object y, SessionFactoryImplementor factory) throws HibernateException {
		return equals(x, y);
	}

	@Override
	public int getHashCode(Object x) throws HibernateException {
		return hashCode(x);
	}

	@Override
	public int getHashCode(Object x, SessionFactoryImplementor factory) throws HibernateException {
		return hashCode(x);
	}

	@Override
	public int compare(Object x, Object y, SessionFactoryImplementor sessionFactoryImplementor) {
		return compare(x, y);
	}

	@Override
	public int compare(Object x, Object y) {
		return IncomparableComparator.INSTANCE.compare(x, y);
	}

	@Override
	public final boolean isDirty(Object old, Object current, SharedSessionContractImplementor session) {
		return isDirty(old, current);
	}

	@Override
	public final boolean isDirty(Object old, Object current, boolean[] checkable,
			SharedSessionContractImplementor session) {
		return checkable[0] && isDirty(old, current);
	}

	protected final boolean isDirty(Object old, Object current) {
		return !isSame(old, current);
	}

	@Override
	public boolean isModified(Object dbState, Object currentState, boolean[] checkable,
			SharedSessionContractImplementor session) throws HibernateException {
		return isDirty(dbState, currentState);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, boolean[] settable,
			SharedSessionContractImplementor session) throws HibernateException, SQLException {
		set(st, returnedClass().cast(value), index, session);
	}

	@Override
	public String toLoggableString(Object value, SessionFactoryImplementor factory) throws HibernateException {
		return String.valueOf(value);
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public Object deepCopy(Object value, SessionFactoryImplementor factory) throws HibernateException {
		return deepCopy(value);
	}

	@Override
	public Serializable disassemble(Object value, SharedSessionContractImplementor session, Object owner)
			throws HibernateException {
		return disassemble(value);
	}

	@Override
	public Object assemble(Serializable cached, SharedSessionContractImplementor session, Object owner)
			throws HibernateException {
		return assemble(cached, session);
	}

	@Override
	public void beforeAssemble(Serializable cached, SharedSessionContractImplementor session) {

	}

	@Override
	public Object replace(Object original, Object target, SharedSessionContractImplementor session, Object owner,
			Map copyCache) throws HibernateException {
		return replace(original, target, owner);
	}

	@Override
	public Object replace(Object original, Object target, SharedSessionContractImplementor session, Object owner,
			Map copyCache, ForeignKeyDirection foreignKeyDirection) throws HibernateException {
		return replace(original, target, owner);
	}

	@Override
	public boolean[] toColumnNullness(Object value, Mapping mapping) {
		return value == null ? ArrayHelper.FALSE : ArrayHelper.TRUE;
	}

	@Override
	public int[] getSqlTypeCodes(Mapping mapping) throws MappingException {
		return new int[]{getSqlType()};
	}

	@Override
	public String toSqlLiteral(T o) {
		return o != null ?
				String.format(Locale.ROOT, "'%s'", o) :
				null;
	}

	@Override
	public String toString(T o) throws HibernateException {
		return o != null ? o.toString() : null;
	}

}
