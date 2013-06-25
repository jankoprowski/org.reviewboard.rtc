package org.reviewboard.rtc.ui;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.ibm.team.foundation.common.util.IMemento;
import com.ibm.team.workitem.ide.ui.internal.aspecteditor.teamoperation.StateBasedModificationEditor;

public class ReviewBoardAspectEditor extends StateBasedModificationEditor {

	private static final int STATE_LABEL = 0;
	private static final int WORKFLOW_ACTION_CHECKBOX = 3;
	private static final String REPOSITORY = "repository";
	private static final String ADDRESS = "address";
	private static final String NAME = "name";
	private Text address;
	private Text name;
	private String addressText = "";
	private String nameText = "";



	@Override
	public void createControl(Composite parent, FormToolkit toolkit) {
		super.createControl(parent, toolkit);

		GridData excludeFromLayout = new GridData();
	    excludeFromLayout.exclude = true;

		Control[] parentsControls = parent.getChildren();
		Label label = (Label) parentsControls[STATE_LABEL];
		label.setText("Trigger review for the following states:");

		Button checkbox = (Button) parentsControls[WORKFLOW_ACTION_CHECKBOX];
		checkbox.setVisible(false);
		checkbox.setLayoutData(excludeFromLayout);

		GridData fillAllColumns = new GridData(GridData.FILL, GridData.CENTER, true, true);
		fillAllColumns.horizontalSpan = 2;

		final ReviewBoardAspectEditor that = this;
		address = toolkit.createText(parent, addressText);
		address.setLayoutData(fillAllColumns);
		address.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent evt) {
				that.setDirty();
			}
		});

		Label addressLabel = toolkit.createLabel(parent, "Enter ReviewBoard's server address like: http://rb.your-company.com/");
		addressLabel.setLayoutData(fillAllColumns);

		name = toolkit.createText(parent, nameText);
		name.setLayoutData(fillAllColumns);
		name.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent evt) {
				that.setDirty();
			}
		});

		Label repositoryLabel = toolkit.createLabel(parent, "Enter ReviewBoard's repository name to use.");
		repositoryLabel.setLayoutData(fillAllColumns);

	}


	@Override
	public boolean saveState(IMemento memento) {
		IMemento reviewboard = memento.createChild(REPOSITORY);
		reviewboard.putString(ADDRESS, address.getText());
		reviewboard.putString(NAME, name.getText());
		return super.saveState(memento);
	}

	@Override
	public void restoreState(IMemento memento) {
		IMemento reviewboard = memento.getChild(REPOSITORY);
		if (reviewboard != null) {
			this.addressText = reviewboard.getString(ADDRESS);
			this.nameText = reviewboard.getString(NAME);
		}
		super.restoreState(memento);
	}
}
