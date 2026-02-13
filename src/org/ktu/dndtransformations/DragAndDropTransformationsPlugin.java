package org.ktu.dndtransformations;

import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.project.ProjectsManager;
import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.magicdraw.ui.dnd.CustomDropDiagramHandlerFactory;
import org.ktu.dndtransformations.ui.TransformationLibraryEventListener;
import org.ktu.dndtransformations.ui.PatternDragAndDropHandler;
import org.ktu.dndtransformations.ui.SetDraggedStereotypeConfigurator;
import org.ktu.dndtransformations.ui.ToolbarConfigurator;

/**
 * Class for registration of Drag and Drop transformations plugin in MagicDraw environment.
 * Extends com.nomagic.magicdraw.plugins.Plugin class
 *
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 */
public class DragAndDropTransformationsPlugin extends Plugin {
    
    /**
     * Performed on plugin close
     */
    @Override
    public boolean close() {
        return true;
    }

    /**
     * Initialize the plugin
     */
    @Override
    public void init() {
        for (int i = 0; i < 10; i++)
            CustomDropDiagramHandlerFactory.register(new PatternDragAndDropHandler(i));
        ActionsConfiguratorsManager configuratorsManager = ActionsConfiguratorsManager.getInstance();
        configuratorsManager.addDiagramToolbarConfigurator("M2M Transformations", new ToolbarConfigurator());
        ProjectsManager manager = Application.getInstance().getProjectsManager();
        manager.addProjectListener(new TransformationLibraryEventListener());
        ActionsConfiguratorsManager.getInstance().addDiagramContextConfigurator("M2M Transformations", new SetDraggedStereotypeConfigurator()); 

    }

    @Override
    public boolean isSupported() {
        return true;
    }

}
