package com.yucl.learn.graph;

import com.structurizr.Workspace;
import com.structurizr.export.Diagram;
import com.structurizr.export.plantuml.C4PlantUMLExporter;
import com.structurizr.export.plantuml.StructurizrPlantUMLExporter;
import com.structurizr.model.*;
import com.structurizr.util.StringUtils;
import com.structurizr.view.*;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestStructurizr {
    public static void main(String[] args) throws Exception {
        TestStructurizr testStructurizr = new TestStructurizr();
        testStructurizr.f();
    }

    private void f() throws IOException {
        Workspace workspace = new Workspace("Getting Started", "This is a model of my software system.");
        Model model = workspace.getModel();

        model.setIdGenerator(new IdGenerator() {
            @Override
            public String generateId(Element element) {
                return   element.getName();
            }

            @Override
            public String generateId(Relationship relationship) {
                return relationship.getSource().getId() + "_" + relationship.getDestination().getId();
            }

            @Override
            public void found(String id) {

            }
        });


        buildModel(workspace, model);


       /* C4PlantUMLWriter c4PlantUMLWriter = new C4PlantUMLWriter();
        FileWriter writer =   new FileWriter("d://c4data/g1");
        c4PlantUMLWriter.write(workspace,writer);
       // c4PlantUMLWriter.write(contextView,writer);
        writer.close();*/

        if (workspace.getViews().isEmpty()) {
            throw new RuntimeException("the workspace contains no views");
        }

        Collection<Diagram> diagrams = export(workspace);
        String outputPath= "d://c4data";
        File outputDir = new File(outputPath);
        outputDir.mkdirs();
        List<VizData> vizData = new ArrayList<>();
        for (Diagram diagram : diagrams) {
            View view = diagram.getView();
            //logger.debug("Writing {}/{}.*", outputPath, getViewName(workspace, diagram.getView()));

            String displayTitle = view.getTitle();
            if (StringUtils.isNullOrEmpty(displayTitle)) {
                displayTitle = view.getName();
            }

            File file = new File(outputPath, String.format("%s.puml", getViewName(workspace, diagram.getView())));
            writeToFile(file, diagram.getDefinition());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            //long plantUmlStart = System.currentTimeMillis();
            SourceStringReader reader = new SourceStringReader(diagram.getDefinition());
            // DiagramDescription desc =
            reader.outputImage(byteArrayOutputStream, new FileFormatOption(FileFormat.SVG));


            String svg = byteArrayOutputStream.toString(StandardCharsets.UTF_8);

            // Remove comments from svg. That reduces size of .svg files and hence the c4viz.json file by 60%...
            // https://stackoverflow.com/a/6806096/345716
            svg = svg.replaceAll( "(?s)<!--.*?-->", "" );

            FileOutputStream fileOutputStream = new FileOutputStream(String.format("%s/%s.svg", outputPath, getViewName(workspace, diagram.getView())));
            fileOutputStream.write(svg.getBytes(StandardCharsets.UTF_8));
            fileOutputStream.close();

            String type = diagram.getView().getClass().getSimpleName();
            String shortName = getViewName(workspace, diagram.getView());

            vizData.add(new VizData(type, shortName, displayTitle, svg, diagram.getDefinition()));
        }


    }

    private void buildModel(Workspace workspace,Model model){
        ViewSet views = workspace.getViews();
        SoftwareSystem softwareSystem = model.addSoftwareSystem(Location.Internal, "The System", "Description");

        SoftwareSystem softwareSystemA = model.addSoftwareSystem("System A", "Description");
        SoftwareSystem softwareSystemB = model.addSoftwareSystem("System B", "Description");
        Person userA = model.addPerson("User A", "Description");
        Person userB = model.addPerson("User B", "Description");

        // userA -> systemA -> system -> systemB -> userB
        userA.uses(softwareSystemA, "");
        softwareSystemA.uses(softwareSystem, "");
        softwareSystem.uses(softwareSystemB, "");
        softwareSystemB.delivers(userB, "");

        // userA -> systemA -> web application -> systemB -> userB
        // web application -> database
        Container webApplication = softwareSystem.addContainer("Web Application", "this is desc", "java");
        Container database = softwareSystem.addContainer("Database", "", "");
        softwareSystemA.uses(webApplication, "");
        webApplication.uses(softwareSystemB, "");
        webApplication.uses(database, "");

        // userA -> systemA -> controller -> service -> repository -> database
        Component controller = webApplication.addComponent("Controller", "");
        Component service = webApplication.addComponent("Service", "");
        Component repository = webApplication.addComponent("Repository", "");
        softwareSystemA.uses(controller, "");
        controller.uses(service, "");
        service.uses(repository, "");
        repository.uses(database, "");

        // userA -> systemA -> controller -> service -> systemB -> userB
        service.uses(softwareSystemB, "");

        ContainerView view = views.createContainerView(softwareSystem, "containers", "Description");

        //view.addNearestNeighbours(webApplication);

        view.addAllElements();


        //view = new ContainerView(softwareSystem, "containers", "Description");
       // view.addNearestNeighbours(softwareSystemA);



       // view = new ContainerView(softwareSystem, "containers", "Description");
       // view.addNearestNeighbours(webApplication);


    }

    private void buildModel3(Workspace workspace, Model model){
        model = workspace.getModel();
        SoftwareSystem softwareSystemA = model.addSoftwareSystem("A", "");
        SoftwareSystem softwareSystemB = model.addSoftwareSystem("B", "");
        SoftwareSystem softwareSystemC1 = model.addSoftwareSystem("C1", "");
        SoftwareSystem softwareSystemC2 = model.addSoftwareSystem("C2", "");
        SoftwareSystem softwareSystemD = model.addSoftwareSystem("D", "");
        SoftwareSystem softwareSystemE = model.addSoftwareSystem("E", "");

        // A -> B -> C1 -> D -> E
        // A -> B -> C2 -> D -> E
        softwareSystemA.uses(softwareSystemB, "uses");
        softwareSystemB.uses(softwareSystemC1, "uses");
        softwareSystemC1.uses(softwareSystemD, "uses");
        softwareSystemB.uses(softwareSystemC2, "uses");
        softwareSystemC2.uses(softwareSystemD, "uses");
        softwareSystemD.uses(softwareSystemE, "uses");

        DynamicView view = workspace.getViews().createDynamicView("key", "Description");

        view.add(softwareSystemA, softwareSystemB);
        view.startParallelSequence();
        view.add(softwareSystemB, softwareSystemC1);
        view.add(softwareSystemC1, softwareSystemD);
        view.endParallelSequence();
        view.startParallelSequence();
        view.add(softwareSystemB, softwareSystemC2);
        view.add(softwareSystemC2, softwareSystemD);
        view.endParallelSequence(true);
        view.add(softwareSystemD, softwareSystemE);

    }


    private void buildModel4(Workspace workspace, Model model){
        ViewSet views = workspace.getViews();
        SoftwareSystem softwareSystem = model.addSoftwareSystem(Location.Internal, "The System", "Description");
        SystemContextView view = views.createSystemContextView(softwareSystem, "context", "Description");

        SoftwareSystem softwareSystemA = model.addSoftwareSystem(Location.External, "System A", "Description");
        SoftwareSystem softwareSystemB = model.addSoftwareSystem(Location.External, "System B", "Description");
        Person userA = model.addPerson(Location.Internal, "User A", "Description");
        Person userB = model.addPerson(Location.External, "User B", "Description");
        Container webApplication = softwareSystem.addContainer("Web Application", "Does something", "Apache Tomcat");
        Container database = softwareSystem.addContainer("Database", "Does something", "MySQL");
        softwareSystemA.uses(webApplication, "");
        webApplication.uses(softwareSystemB, "");
        webApplication.uses(database, "");


        softwareSystemB.setGroup("aaa");
        userB.setGroup("aaa");

        model.setEnterprise(new Enterprise("adsfasdfasd"));

        softwareSystem.uses(softwareSystemA,"dsfasdsadf");

        userA.uses(softwareSystem,"asds");
        userA.uses(webApplication,"aaaa");
        userB.uses(softwareSystemA,"bbbb");
        userB.uses(softwareSystemB,"bbbb");
        view.addAllElements();

        ContainerView containerView = views.createContainerView(softwareSystem,"container","test");
        containerView.addAllElements();


    }

    private void buildModel2(Workspace workspace, Model model) {
        ViewSet views = workspace.getViews();
        SoftwareSystem softwareSystem = model.addSoftwareSystem("SoftwareSystem", "My software system.");
        Person user = model.addPerson("User", "A user of my software system.");

        user.uses(softwareSystem, "Uses");

        SystemContextView contextView = views.createSystemContextView(softwareSystem, "SystemContext", "An example of a System Context diagram.");
        contextView.addAllSoftwareSystems();
        contextView.addAllPeople();
    }


    private Collection<Diagram> export(Workspace workspace) {
        C4PlantUMLExporter exporter = new C4PlantUMLExporter();

        workspace
                .getViews()
                .getConfiguration()
                .addProperty(StructurizrPlantUMLExporter.PLANTUML_SEQUENCE_DIAGRAMS_PROPERTY, "false");
        if (workspace == null) {
            throw new IllegalArgumentException("A workspace must be provided.");
        }

        Collection<Diagram> diagrams = new ArrayList<>();

        for (SystemLandscapeView view : workspace.getViews().getSystemLandscapeViews()) {
            setSystemLandscapeElementURLs(workspace, view);
            Diagram diagram = exporter.export(view);
            resetElementURLs(view);
            if (diagram != null) {
                diagrams.add(diagram);
            }
        }

        for (SystemContextView view : workspace.getViews().getSystemContextViews()) {
            setSystemContextElementURLs(workspace, view);
            Diagram diagram = exporter.export(view);
            resetElementURLs(view);
            if (diagram != null) {
                diagrams.add(diagram);
            }
        }

        for (ContainerView view : workspace.getViews().getContainerViews()) {
            setContainerElementURLs(workspace, view);
            Diagram diagram = exporter.export(view);
            resetElementURLs(view);
            if (diagram != null) {
                diagrams.add(diagram);
            }
        }

        for (ComponentView view : workspace.getViews().getComponentViews()) {
            Diagram diagram = exporter.export(view);
            if (diagram != null) {
                diagrams.add(diagram);
            }
        }

        for (DynamicView view : workspace.getViews().getDynamicViews()) {
            Diagram diagram = exporter.export(view);
            if (diagram != null) {
                diagrams.add(diagram);
            }
        }

        for (DeploymentView view : workspace.getViews().getDeploymentViews()) {
            Diagram diagram = exporter.export(view);
            if (diagram != null) {
                diagrams.add(diagram);
            }
        }

        return diagrams;
    }

    private void setSystemLandscapeElementURLs(Workspace workspace, SystemLandscapeView view) {
        Collection<SystemContextView> views = view.getViewSet().getSystemContextViews();
        for (ElementView elementView : view.getElements()) {
            Element element = elementView.getElement();
            if (element instanceof SoftwareSystem) {
                for (View alternateView : views) {
                    if (alternateView.getSoftwareSystem() == element) {
                        element.setUrl("https://view/" + getViewName(workspace, alternateView));
                    }
                }
            }
        }
    }

    private void setSystemContextElementURLs(Workspace workspace, SystemContextView view) {
        Collection<ContainerView> views = view.getViewSet().getContainerViews();
        for (ElementView elementView : view.getElements()) {
            Element element = elementView.getElement();
            if (element instanceof SoftwareSystem) {
                for (View alternateView : views) {
                    if (alternateView.getSoftwareSystem() == element) {
                        element.setUrl("https://view/" + getViewName(workspace, alternateView));
                    }
                }
            }
        }
    }

    private void setContainerElementURLs(Workspace workspace, ContainerView view) {
        Collection<ComponentView> views = view.getViewSet().getComponentViews();
        for (ElementView elementView : view.getElements()) {
            Element element = elementView.getElement();
            if (element instanceof Container) {
                for (ComponentView alternateView : views) {
                    if (alternateView.getContainer() == element) {
                        element.setUrl("https://view/" + getViewName(workspace, alternateView));
                    }
                }
            }
        }
    }

    private void resetElementURLs(View view) {
        for (ElementView elementView : view.getElements()) {
            Element element = elementView.getElement();
            element.setUrl(null);
            // Older versions of setUrl ignore element.setUrl(null) - make sure this version doesn't
            // See Issue #169: Feature Request: Allow API to set ModelItem#url = null
            // https://github.com/structurizr/java/issues/169
            if (element.getUrl() != null) {
                throw new RuntimeException("Couldn't element.setUrl(null)");
            }
        }
    }

    private void addDefaultViewsAndStyles(Workspace workspace) {
        if (workspace.getViews().isEmpty()) {
            //logger.debug("no views defined; creating default views");
            workspace.getViews().createDefaultViews();
        }

        Styles styles = workspace.getViews().getConfiguration().getStyles();
        if (styles.getElements().isEmpty() && styles.getRelationships().isEmpty() && workspace.getViews().getConfiguration().getThemes() == null) {
           // logger.debug("no styles or themes defined; use the \"default\" theme to add some default styles");
        }
    }

    String getViewName(Workspace workspace, View view) {
        long workspaceId = workspace.getId();
        if (workspaceId > 0) {
            return String.format("%d-%s", workspaceId, view.getKey());
        } else {
            return view.getKey();
        }
    }

    private void writeToFile(File file, String content) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
        writer.write(content);
        writer.close();
    }

}
