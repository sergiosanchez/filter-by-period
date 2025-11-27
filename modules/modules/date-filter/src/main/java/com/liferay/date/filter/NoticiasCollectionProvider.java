/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 * <p>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.date.filter;

import com.liferay.asset.kernel.service.AssetTagLocalService;
import com.liferay.document.library.kernel.model.DLFileEntryConstants;
import com.liferay.info.collection.provider.CollectionQuery;
import com.liferay.info.collection.provider.FilteredInfoCollectionProvider;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.info.collection.provider.SingleFormVariationInfoCollectionProvider;
import com.liferay.info.filter.CategoriesInfoFilter;
import com.liferay.info.filter.InfoFilter;
import com.liferay.info.pagination.InfoPage;
import com.liferay.info.pagination.Pagination;
import com.liferay.info.sort.Sort;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.BooleanClause;
import com.liferay.portal.kernel.search.BooleanClauseFactoryUtil;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.RangeTermFilter;
import com.liferay.portal.kernel.search.generic.BooleanQueryImpl;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.io.Serializable;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


@Component(immediate = true, service = InfoCollectionProvider.class)
public class NoticiasCollectionProvider
	implements FilteredInfoCollectionProvider<JournalArticle>,
			   SingleFormVariationInfoCollectionProvider<JournalArticle> {



	@Override
	public InfoPage<JournalArticle> getCollectionInfoPage(
		CollectionQuery collectionQuery) {	

		return _getArticleInfoPage(collectionQuery);
	}

	@Override
	public String getFormVariationKey() {
		//TODO Reemplazar por configuración y no meter el srtructureId a fuego
		return String.valueOf(34030);
	}

	@Override
	public String getLabel(Locale locale) {
		return LanguageUtil.get(locale, "noticias");
	}

	@Override
	public List<InfoFilter> getSupportedInfoFilters() {
		return Arrays.asList(new CategoriesInfoFilter(), new DateInfoFilter());
	}

	private SearchContext _buildSearchContext(CollectionQuery collectionQuery) {
		SearchContext searchContext = new SearchContext();

		searchContext.setAndSearch(true);
		searchContext.setAttributes(
			HashMapBuilder.<String, Serializable>put(
				Field.STATUS, WorkflowConstants.STATUS_APPROVED
			).put(
				"ddmStructureId", getFormVariationKey()
			).put(
				"head", true
			).put(
				"latest", true
			).build());

		CategoriesInfoFilter categoriesInfoFilterOptional =
			collectionQuery.getInfoFilter(CategoriesInfoFilter.class);

		if (categoriesInfoFilterOptional != null) {
			CategoriesInfoFilter categoriesInfoFilter =
				categoriesInfoFilterOptional;

			long[] categoryIds = ArrayUtil.append(
				categoriesInfoFilter.getCategoryIds());

			categoryIds = ArrayUtil.unique(categoryIds);

			searchContext.setAssetCategoryIds(categoryIds);
		}

		DateInfoFilter dateInfoFilterOptional =
			collectionQuery.getInfoFilter(DateInfoFilter.class);

		if (dateInfoFilterOptional != null) {
			searchContext.setBooleanClauses(
				_getDateBooleanClause(dateInfoFilterOptional));
		}

		searchContext.setClassTypeIds(
			new long[] {GetterUtil.getLong(getFormVariationKey())});

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		searchContext.setCompanyId(serviceContext.getCompanyId());

		Pagination pagination = collectionQuery.getPagination();

		searchContext.setEnd(pagination.getEnd());

		searchContext.setEntryClassNames(
			new String[] {JournalArticle.class.getName()});
		searchContext.setGroupIds(
			new long[] {serviceContext.getScopeGroupId()});

		Sort sortOptional = collectionQuery.getSort();

		if (sortOptional != null) {
			Sort sort = sortOptional;

			searchContext.setSorts(
				new com.liferay.portal.kernel.search.Sort(
					sort.getFieldName(),
					com.liferay.portal.kernel.search.Sort.LONG_TYPE,
					sort.isReverse()));
		}
		else {
			searchContext.setSorts(
				new com.liferay.portal.kernel.search.Sort(
					Field.MODIFIED_DATE,
					com.liferay.portal.kernel.search.Sort.LONG_TYPE, true));
		}

		searchContext.setStart(pagination.getStart());

		QueryConfig queryConfig = searchContext.getQueryConfig();

		queryConfig.setHighlightEnabled(false);
		queryConfig.setScoreEnabled(false);

		return searchContext;
	}

	private BooleanClause[] _getDateBooleanClause(
	    DateInfoFilter infoFilterDate) {

	    BooleanQueryImpl booleanQueryImpl = new BooleanQueryImpl();
	    BooleanFilter booleanFilter = new BooleanFilter();

	    // 1. Obtener la clave del período (ej: "lastMonth")
	    String periodKey = infoFilterDate.getPeriodKey(); 
	    
	    // 2. Definir el Calendario y el rango
	    Calendar calendar = Calendar.getInstance();
	    Date endDate = calendar.getTime(); // Hoy es el final del rango
	    
	    // Calcular la fecha de inicio
	    switch (periodKey) {
	    	case "lastDay":
	    		calendar.add(Calendar.DAY_OF_YEAR, -1);
	    		break;
	        case "lastMonth":
	            calendar.add(Calendar.MONTH, -1);
	            break;
	        case "last3Months":
	            calendar.add(Calendar.MONTH, -3);
	            break;
	        case "lastYear":
	            calendar.add(Calendar.YEAR, -1);
	            break;
	        default:
	            // Si no se selecciona un periodo, no se aplica el filtro de fecha
	            return new BooleanClause[0]; 
	    }
	    
	    Date startDate = calendar.getTime();
	    
	    // El formato debe ser el índice de Elasticsearch, que es 'yyyyMMddHHmmss'
	    Format format = FastDateFormatFactoryUtil.getSimpleDateFormat(
	        PropsUtil.get(PropsKeys.INDEX_DATE_FORMAT_PATTERN));

	    // El RangeTermFilter busca Assets creados entre startDate (inclusive) y endDate (inclusive)
	    RangeTermFilter rangeTermFilter = new RangeTermFilter(
	        Field.CREATE_DATE, true, true, format.format(startDate),
	        format.format(endDate));

	    booleanFilter.add(rangeTermFilter, BooleanClauseOccur.MUST); // Usamos MUST para restringir

	    booleanQueryImpl.setPreBooleanFilter(booleanFilter);

	    return new BooleanClause[] {
	        BooleanClauseFactoryUtil.create(
	            booleanQueryImpl, BooleanClauseOccur.MUST.getName())
	    };
	}

	private InfoPage<JournalArticle> _getArticleInfoPage(
		CollectionQuery collectionQuery) {


		try {
			
			Indexer<?> indexer = IndexerRegistryUtil.getIndexer(
				JournalArticle.class.getName());

			SearchContext searchContext = _buildSearchContext(collectionQuery);

			Hits hits = indexer.search(searchContext);

			List<JournalArticle> articles = new ArrayList<>();

			for (Document document : hits.getDocs()) {
				String articleId = document.get(Field.ARTICLE_ID);
				
				long groupId = GetterUtil.getLong(
						document.get(Field.GROUP_ID));

				articles.add(_journalArticleLocalService.getLatestArticle(groupId, articleId));
			}	
			
			
			
			return InfoPage.of(
					articles, collectionQuery.getPagination(), hits.getLength());

			
		}
		catch (PortalException portalException) {
			if (_log.isWarnEnabled()) {
				_log.warn(portalException);
			}

			return null;
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		NoticiasCollectionProvider.class);

	@Reference
	private AssetTagLocalService _assetTagLocalService;

	@Reference
	private JournalArticleLocalService _journalArticleLocalService;
	


}