package org.zenframework.z8.server.apidocs;

import org.zenframework.z8.server.apidocs.dto.BaseInfo;
import org.zenframework.z8.server.apidocs.dto.Documentation;
import org.zenframework.z8.server.apidocs.field_extractor.FieldExtractor;
import org.zenframework.z8.server.apidocs.field_extractor.FieldExtractorFactory;
import org.zenframework.z8.server.apidocs.actions.*;
import org.zenframework.z8.server.apidocs.dto.Entity;
import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

import java.util.*;

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
            Query query = (Query) entity.newInstance();

            Entity entityResult = new Entity();
            entityResult.setEntityName(query.getAttribute("name"));
            entityResult.setEntityDescription(query.getAttribute(Json.apiDescription.toString()));
            entityResult.setEntityId(entity.classId());
            entityResult.setContentParams(entity.getAttribute("contentParams"));
            for(Action action : query.actions()) {
                entityResult.getActionsNames().add(new BaseInfo(action.id(), action.displayName()));
            }

            for(IClass<? extends IObject> member : query.objects) {
                if (member instanceof Query.CLASS) {
                    entityResult.getRelatedEntities().add(new BaseInfo(member.index(), member.getAttribute("name")));
                }
            }

            // set a list of available fields of the entity
            for(Field field : query.getDataFields()) {
                if (field.hasAttribute(Json.apiDescription.toString())) {
                    FieldExtractor fieldExtractor = FieldExtractorFactory.getExtractor(field);
                    entityResult.getEntityFields().add(fieldExtractor.extract(field));
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
