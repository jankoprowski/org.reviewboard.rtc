package org.reviewboard.rtc.ui;

import com.ibm.team.process.ide.ui.IProcessAspectEditorFactory;
import com.ibm.team.process.ide.ui.ProcessAspectEditor;

public class ReviewBoardAspectEditorFactory implements IProcessAspectEditorFactory {

	public ReviewBoardAspectEditorFactory() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ProcessAspectEditor createProcessAspectEditor(String processAspectId) {
		return new ReviewBoardAspectEditor();
	}

}
