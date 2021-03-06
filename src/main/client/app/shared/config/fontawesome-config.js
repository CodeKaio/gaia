import Vue from 'vue';
import { library } from '@fortawesome/fontawesome-svg-core';
import {
  faArchive,
  faAngleDoubleDown,
  faAngleDoubleLeft,
  faAngleDoubleRight,
  faAngleDoubleUp,
  faCalendarAlt,
  faCaretSquareUp,
  faChevronDown,
  faChevronUp,
  faCircleNotch,
  faCog,
  faDollarSign,
  faEdit,
  faFile,
  faFolder,
  faHistory,
  faInfo,
  faLayerGroup,
  faLock,
  faMinus,
  faObjectGroup,
  faPlay,
  faPlus,
  faPollH,
  faRedo,
  faRocket,
  faSave,
  faSignOutAlt,
  faSitemap,
  faStarOfLife,
  faStop,
  faStopCircle,
  faStopwatch,
  faTachometerAlt,
  faTag,
  faUpload,
  faUser,
  faUserAlt,
  faUserMinus,
  faUserPlus,
  faUserFriends,
  faUserShield,
} from '@fortawesome/free-solid-svg-icons';
import {
  faGithub,
  faGitlab,
  faMarkdown,
  faSuperpowers,
} from '@fortawesome/free-brands-svg-icons';
import {
  faCheckSquare as farCheckSquare,
  faCopy as farCopy,
  faUser as farUser,
  faTrashAlt as farTrashAlt,
} from '@fortawesome/free-regular-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

export default {
  init: () => {
    library.add(
      faArchive,
      faCalendarAlt,
      faDollarSign,
      faEdit,
      faUser,
      faUserAlt,
      faUserMinus,
      faUserPlus,
      faLock,
      faGithub,
      faGitlab,
      faHistory,
      faMarkdown,
      faMinus,
      faPlus,
      faSave,
      faTachometerAlt,
      farTrashAlt,
      faObjectGroup,
      faLayerGroup,
      faCog,
      faUserFriends,
      faTag,
      faSignOutAlt,
      faStopwatch,
      faSuperpowers,
      farUser,
      faAngleDoubleLeft,
      faAngleDoubleRight,
      faInfo,
      faRocket,
      faCaretSquareUp,
      faSave,
      faSitemap,
      faStopCircle,
      faUpload,
      faUserShield,
      faStarOfLife,
      farCheckSquare,
      faFolder,
      faFile,
      faPlay,
      faStop,
      faRedo,
      farCopy,
      faAngleDoubleUp,
      faAngleDoubleDown,
      faChevronDown,
      faChevronUp,
      faCircleNotch,
      faPollH,
    );
    Vue.component('font-awesome-icon', FontAwesomeIcon);
  },
};
