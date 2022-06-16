package com.test.collection.providers;

import com.liferay.info.collection.provider.CollectionQuery;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.info.pagination.InfoPage;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalFolder;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.journal.service.JournalFolderLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.ResourceBundleUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = InfoCollectionProvider.class)

public class MyCollectionProvider implements InfoCollectionProvider<JournalArticle> {
	
	@Reference
	private JournalArticleLocalService _journalArticleLocalService;

	@Reference
	private JournalFolderLocalService _journalFolderLocalService;
	
	private static final Log logr = LogFactoryUtil.getLog(MyCollectionProvider.class);

	@Override
	public String getLabel(Locale locale) {
		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(locale, getClass());
		return LanguageUtil.get(resourceBundle, "my-collection-provider");
	}

	@Override
	public InfoPage<JournalArticle> getCollectionInfoPage(CollectionQuery collectionQuery) {
		ServiceContext sc = ServiceContextThreadLocal.getServiceContext();

		// Find folder
		JournalFolder folder = _journalFolderLocalService.fetchFolder(sc.getScopeGroupId(), 
				"Highlights");
		
		// Get contents list
		List<JournalArticle> articles = _journalArticleLocalService.getArticles(sc.getScopeGroupId(),
				folder.getFolderId());
		
		List<JournalArticle> latestArticles = new ArrayList<JournalArticle>();
		
		for ( JournalArticle ja : articles ) {
			try {
				if ( _journalArticleLocalService.isLatestVersion(ja.getGroupId(), ja.getArticleId(), ja.getVersion()) ) {
					latestArticles.add(ja);
				}
			} catch (PortalException e) {
				logr.error(e);
			}
		}
		return InfoPage.of(latestArticles, collectionQuery.getPagination(), latestArticles.size());
	}
	
}

