/*
 * Copyright (C) 2016 CodeFireUA <edu@codefire.com.ua>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package javasync.net;

/**
 *
 * @author CodeFireUA <edu@codefire.com.ua>
 */
public interface LinkStoreListener {

    public void downloadBegin(DownloadTask.Descriptor task);

    public void downloadProgress(DownloadTask.Descriptor task);

    public void downloadComplete(DownloadTask.Descriptor task);

}
