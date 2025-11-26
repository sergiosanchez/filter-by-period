package com.liferay.date.filter;

import com.liferay.info.filter.InfoFilter;


public class DateInfoFilter implements InfoFilter {

	public static final String FILTER_TYPE_NAME = "period";

	/**
	 * Reemplazar getDate() por getPeriodKey()
	 */
	public String getPeriodKey() {
		return _periodKey;
	}

	@Override
	public String getFilterTypeName() {
		return FILTER_TYPE_NAME;
	}

	/**
	 * Reemplazar setDate(Date date) por setPeriodKey(String periodKey)
	 */
	public void setPeriodKey(String periodKey) {
		_periodKey = periodKey;
	}

	/**
	 * Cambiar la variable privada de Date a String
	 */
	private String _periodKey;

}