package org.zenframework.z8.server.apidocs;

import org.zenframework.z8.server.apidocs.actions.*;
import org.zenframework.z8.server.apidocs.dto.*;
import org.zenframework.z8.server.apidocs.field.extractor.FieldExtractor;
import org.zenframework.z8.server.apidocs.field.extractor.FieldExtractorFactory;
import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.form.action.Parameter;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DocBuilder {

    public Documentation build (Collection<OBJECT.CLASS<? extends OBJECT>> entities) {
        Documentation doc = new Documentation();
        doc.setEntities(collectEntities(entities));
        doc.setActions(getActionsDescription(entities));
        return doc;
    }

    protected List<Entity> collectEntities(Collection<OBJECT.CLASS<? extends OBJECT>> entities) {
        List<Entity> entitiesDescription = new ArrayList<>();
        for(OBJECT.CLASS<? extends OBJECT> entity : entities) {
            OBJECT z8Object = entity.newInstance();
            Entity entityResult = new Entity();
            entityResult.setEntityName(z8Object.name());
            entityResult.setEntityDescription(z8Object.getAttribute(Json.apiDescription.toString()));
            entityResult.setEntityId(z8Object.classId());
            entityResult.setContentParams(z8Object.getAttribute("apiActions"));
            entityResult.setRequestAttributes(z8Object.getAttribute("requestAttributesDescription"));
            for(IClass<? extends IObject> member : z8Object.objects) {
                if (member instanceof Query.CLASS) {
                    entityResult.getRelatedEntities().add(new BaseInfo(member.index(), member.getAttribute("name")));
                }
            }
            if (z8Object instanceof Query) {
                Query query = (Query) z8Object;
                registerActions(entityResult, query);

                // set a list of available fields of the entity
                for (Field field : query.getDataFields()) {
                    if (field.hasAttribute(Json.apiDescription.toString())) {
                        FieldExtractor fieldExtractor = FieldExtractorFactory.getExtractor(field);
                        entityResult.getEntityFields().add(fieldExtractor.extract(field));
                    }
                }
            }
            entitiesDescription.add(entityResult);
        }
        return entitiesDescription;
    }

    private void registerActions(Entity entityResult, Query query) {
        for (Action action : query.actions()) {
            EntityAction entityAction = new EntityAction(action.id(), action.displayName());
            entityResult.getEntityActions().add(entityAction);
            for (Parameter.CLASS<Parameter> parameterWrap : action.parameters())
                entityAction.getParameters().add(new FieldDescription(parameterWrap.get().id(), parameterWrap.get().getType().toString(), null));
        }
    }

    protected List<IActionRequest> getActionsDescription(Collection<OBJECT.CLASS<? extends OBJECT>> entities) {
        List<IActionRequest> actions = new ArrayList<>();
        for (OBJECT.CLASS<? extends OBJECT> entity : entities) {
            if (entity.newInstance() instanceof Query && entity.getAttribute("generatable") != null) {
                Query query = (Query) entity.newInstance();
                IActionRequest[] availableActions = new IActionRequest[]{
                        new CreateAction(),
                        new CopyAction(),
                        new ReadAction(),
                        new UpdateAction(),
                        new DestroyAction(),
                        new CommandAction(),
                        new ExportAction(),
                        new AttachAction(),
                        new DetachAction()
                };
                for (IActionRequest action : availableActions) {
                    action.makeExample(query);
                    actions.add(action);
                }
                break;
            }
        }
        return actions;
    }
}
