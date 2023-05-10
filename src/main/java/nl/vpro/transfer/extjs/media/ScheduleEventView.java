/*
 * Copyright (C) 2009 All rights reserved
 * VPRO The Netherlands
 */
package nl.vpro.transfer.extjs.media;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import javax.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;
import nl.vpro.domain.media.*;
import nl.vpro.jackson2.DurationToJsonTimestamp;
 
import nl.vpro.domain.user.Broadcaster;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "channel",
        "channelText",
        "net",
        "netText",
        "start",
        "duration",
        "mediaId",
        "title",
        "description",
        "isRerun",
        "broadcasters"
        })
public class ScheduleEventView {

    @Getter
    private String channel;

    @Getter
    private String channelText;

    @Getter
    private String net;

    @Getter
    private String netText;

    @Getter
    @Setter
    private Instant start;

    @JsonSerialize(using = DurationToJsonTimestamp.Serializer.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = DurationToJsonTimestamp.Deserializer.class)
    @Getter
    private Duration duration;

    @Getter
    @Setter
    private Long mediaId;

    @Getter
    private String title;

    @Getter
    private String description;

    @Getter
    @Setter
    private boolean isRerun = false;

    @XmlElementWrapper(name = "broadcasters")
    @XmlElement(name = "broadcaster")
    @Getter
    private final List<String> broadcasters = new ArrayList<>();

    private ScheduleEventView() {
    }

    private ScheduleEventView(Channel channel, Net net, Instant start) {
        this.channel = channel.name();
        this.channelText = channel.toString();
        this.net = net == null ? null  : net.getId();
        this.netText = net == null ? null : net.toString();
        this.start = start;
    }

    public static ScheduleEventView createScheduleEvent(ScheduleEvent fullEvent) {
        MediaObject media = fullEvent.getParent();

        ScheduleEventView simpleEvent = new ScheduleEventView(
            fullEvent.getChannel(), fullEvent.getNet(),
            fullEvent.getStartInstant()
        );

        if(media != null) {
            simpleEvent.mediaId = media.getId();
            simpleEvent.title = media.getMainTitle();
            simpleEvent.description = media.getMainDescription();
            if (simpleEvent.description == null) {
                //https://jira.vpro.nl/browse/MSE-1836
                simpleEvent.description = "";
            }
        }

        simpleEvent.duration = fullEvent.getDuration();
        simpleEvent.setRerunEvent(fullEvent);


        for(Broadcaster broadcaster : fullEvent.getParent().getBroadcasters()) {
            simpleEvent.broadcasters.add(broadcaster.getDisplayName());
        }

        return simpleEvent;
    }

    public static ScheduleEventView createMediaEvent(ScheduleEvent fullEvent) {
        if (fullEvent == null) {
            return null;
        }
        ScheduleEventView simpleEvent = new ScheduleEventView(
            fullEvent.getChannel(), fullEvent.getNet(),
            fullEvent.getStartInstant()
        );

        simpleEvent.duration = fullEvent.getDuration();
        simpleEvent.setRerunEvent(fullEvent);

        List<Broadcaster> bc = fullEvent.getParent() == null ? null : fullEvent.getParent().getBroadcasters();
        if(bc != null) {
            for(Broadcaster broadcaster : bc) {
                simpleEvent.broadcasters.add(broadcaster.getDisplayName());
            }
        }

        return simpleEvent;
    }

    private void setRerunEvent(ScheduleEvent fullEvent) {
        this.isRerun = ScheduleEvents.isRerun(fullEvent);
    }
   
}
