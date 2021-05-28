package org.zenframework.z8.server.apidocs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.apidocs.actions.AttachAction;
import org.zenframework.z8.server.apidocs.actions.CommandAction;
import org.zenframework.z8.server.apidocs.actions.CopyAction;
import org.zenframework.z8.server.apidocs.actions.CreateAction;
import org.zenframework.z8.server.apidocs.actions.DestroyAction;
import org.zenframework.z8.server.apidocs.actions.DetachAction;
import org.zenframework.z8.server.apidocs.actions.ExportAction;
import org.zenframework.z8.server.apidocs.actions.ReadAction;
import org.zenframework.z8.server.apidocs.actions.UpdateAction;
import org.zenframework.z8.server.apidocs.dto.BaseInfo;
import org.zenframework.z8.server.apidocs.dto.Documentation;
import org.zenframework.z8.server.apidocs.dto.Entity;
import org.zenframework.z8.server.apidocs.field.extractor.FieldExtractor;
import org.zenframework.z8.server.apidocs.field.extractor.FieldExtractorFactory;
import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

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
                for (Action action : query.actions()) {
                    entityResult.getActionsNames().add(new BaseInfo(action.id(), action.displayName()));
                }

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

    protected List<IActionRequest> getActionsDescription(Collection<OBJECT.CLASS<? extends OBJECT>> entities) {
        List<IActionRequest> actions = new ArrayList<>();
        if (entities.iterator().hasNext()) {
            Query query = (Query) entities.iterator().next().newInstance();
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
        }
        return actions;
    }
}
