package com.zergatul.cheatutils.webui;

public class AutoDropApi extends ApiBase {

    @Override
    public String getRoute() {
        return "auto-drop";
    }

    /*@Override
    public String get() throws HttpException {
        return gson.toJson(ConfigStore.instance.getConfig().autoDropConfig.items);
    }

    @Override
    public String post(String id) throws HttpException {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (item == null) {
            return gson.toJson(false);
        }

        List<Item> items = ConfigStore.instance.getConfig().autoDropConfig.items;
        if (items.stream().anyMatch(i -> i == item)) {
            return gson.toJson(false);
        }

        items.add(item);
        ConfigStore.instance.requestWrite();

        return gson.toJson(true);
    }

    @Override
    public String delete(String id) throws HttpException {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (item == null) {
            return gson.toJson(false);
        }

        List<Item> items = ConfigStore.instance.getConfig().autoDropConfig.items;
        return gson.toJson(items.remove(item));
    }*/
}