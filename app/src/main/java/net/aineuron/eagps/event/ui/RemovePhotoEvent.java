package net.aineuron.eagps.event.ui;

import net.aineuron.eagps.model.database.order.Photo;

import java.util.List;

/**
 * Created by Vit Veres on 10.08.2017
 * as a part of eagps project.
 */

public class RemovePhotoEvent {
    public final List<Photo> photos;
    public final String photoPath;

    public RemovePhotoEvent(List<Photo> photos, String photoPath) {
        this.photoPath = photoPath;
        this.photos = photos;
    }
}
