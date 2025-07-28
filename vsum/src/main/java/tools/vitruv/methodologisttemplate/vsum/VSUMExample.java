package tools.vitruv.methodologisttemplate.vsum;

import tools.vitruv.framework.vsum.VirtualModelBuilder;
import tools.vitruv.methodologisttemplate.model.model.ModelFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.emf.common.util.URI;

import mir.reactions.model2Model2.Model2Model2ChangePropagationSpecification;
import tools.vitruv.change.testutils.TestUserInteraction;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.views.ViewTypeFactory;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.framework.remote.server.*;

/**
 * This class provides an example how to define and use a VSUM.
 */
public class VSUMExample {
    public static void main(String[] args) {
        System.out.println("Starting VSUM Example...");
        VirtualModelInitializer initializer = () -> {
            VirtualModel vsum = createDefaultVirtualModel();
            addSystem(vsum, Path.of("vsumexample"));
            return vsum;
        };
        try {
            VitruvServer server = new VitruvServer(initializer, 8080, "0.0.0.0");
            server.start();
            System.out.println("VSUM Example started successfully.");
        } catch (IOException e) {
            System.err.println("Failed to start the Vitruv server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static VirtualModel createDefaultVirtualModel() {
        return new VirtualModelBuilder()
                .withStorageFolder(Path.of("vsumexample"))
                .withUserInteractorForResultProvider(new TestUserInteraction.ResultProvider(new TestUserInteraction()))
                .withChangePropagationSpecifications(new Model2Model2ChangePropagationSpecification())
                .withViewType(ViewTypeFactory.createIdentityMappingViewType("default"))
                .buildAndInitialize();
    }

    private static View getDefaultView(VirtualModel vsum, Collection<Class<?>> rootTypes) {
        var selector = vsum.createSelector(ViewTypeFactory.createIdentityMappingViewType("default"));
        selector.getSelectableElements().stream()
                .filter(element -> rootTypes.stream().anyMatch(it -> it.isInstance(element)))
                .forEach(it -> selector.setSelected(it, true));
        return selector.createView();
    }

    private static void modifyView(CommittableView view, Consumer<CommittableView> modificationFunction) {
        modificationFunction.accept(view);
        view.commitChanges();
    }

    private static void addSystem(VirtualModel vsum, Path projectPath) {
        System.out.println(ModelFactory.eINSTANCE.createSystem());
        CommittableView view = getDefaultView(vsum, List.of(System.class)).withChangeDerivingTrait();
        modifyView(view, (CommittableView v) -> {
            v.registerRoot(
                    ModelFactory.eINSTANCE.createSystem(),
                    URI.createFileURI(projectPath.toString() + "/example.model"));
        });
    }

}
