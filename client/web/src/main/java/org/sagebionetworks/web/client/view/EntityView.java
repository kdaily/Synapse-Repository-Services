package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface EntityView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
		
	public interface Presenter {

		PlaceChanger getPlaceChanger();
	}

	/**
	 * Set entity to display
	 * @param entity
	 */
	public void setEntity(Entity entity);

}
