/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.nogoservice.service;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Klaus Groenbaek
 *         Created 05/05/17.
 */
public class PolygonTransformerTest {

    /**
     * NoGo area at the Swedish area of Flintrannen which has a self intersecting are
     */
    @Test
    public void invalidPolygon() throws Exception {

        String wkt = "POLYGON ((12.719870769039025 55.516605325730765, 12.723819061395844 55.52110069015511, 12.724477110121981 55.52192636933509, 12.725957719755788 55.52311901703951, 12.728260890297268 55.52458689113726, 12.737967109007785 55.53045838752825, 12.777121008212923 55.55467831014107, 12.777943569120593 55.55486179440329, 12.779259666572866 55.55541224718994, 12.780246739662072 55.55577921571438, 12.78139832493281 55.55642141063215, 12.781891861477414 55.55688012128769, 12.782549910203551 55.556696637025475, 12.784524056381962 55.557889284729896, 12.785511129471166 55.55807276899211, 12.786662714741905 55.55862322177877, 12.786827226923439 55.558898448172094, 12.787156251286508 55.558898448172094, 12.787156251286508 55.55908193243431, 12.78764978783111 55.558990190303206, 12.78764978783111 55.55880670604098, 12.787814300012645 55.558990190303206, 12.788307836557246 55.558990190303206, 12.788307836557246 55.55935715882764, 12.788636860920315 55.55926541669653, 12.788965885283384 55.559632385220965, 12.789788446191055 55.56027458013873, 12.790117470554124 55.56018283800762, 12.791269055824863 55.5609167750565, 12.791598080187931 55.5609167750565, 12.791927104551 55.56128374358093, 12.792256128914067 55.56128374358093, 12.792256128914067 55.56146722784315, 12.79274966545867 55.56137548571204, 12.793243202003273 55.56146722784315, 12.793901250729409 55.56201768062981, 12.794065762910943 55.561834196367585, 12.794559299455546 55.561925938498696, 12.794559299455546 55.56210942276091, 12.79505283600015 55.56220116489202, 12.796862469997025 55.563393812596445, 12.797191494360094 55.563393812596445, 12.79752051872316 55.56385252325199, 12.798014055267764 55.563760781120884, 12.798836616175436 55.564127749645316, 12.799165640538504 55.56421949177643, 12.800481737990777 55.56476994456308, 12.80097527453538 55.56476994456308, 12.801468811079982 55.56504517095641, 12.803113932895323 55.5659625922675, 12.804594542529133 55.5669717557097, 12.805417103436803 55.5669717557097, 12.80607515216294 55.56752220849636, 12.806568688707543 55.56761395062747, 12.807062225252144 55.567797434889684, 12.807391249615213 55.56816440341412, 12.807884786159816 55.56816440341412, 12.808378322704419 55.56843962980745, 12.809365395793623 55.568990082594105, 12.81002344451976 55.56917356685632, 12.810187956701295 55.56944879324965, 12.810516981064362 55.56944879324965, 12.810516981064362 55.56963227751187, 12.811010517608965 55.56963227751187, 12.811833078516637 55.570090988167415, 12.811668566335102 55.57027447242963, 12.811504054153568 55.57027447242963, 12.811010517608965 55.57064144095407, 12.806733200889076 55.57275150996958, 12.849835392451034 55.59935672799124, 12.853290148263252 55.597613627500166, 12.853290148263252 55.59752188536906, 12.85477075789706 55.596787948320184, 12.855264294441662 55.59706317471351, 12.85559331880473 55.59706317471351, 12.855099782260128 55.59669620618908, 12.856086855349334 55.596145753402425, 12.85674490407547 55.5959622691402, 12.857238440620073 55.59632923766464, 12.857567464983141 55.59632923766464, 12.857731977164676 55.59651272192686, 12.857073928438538 55.59660446405797, 12.857731977164676 55.596787948320184, 12.85789648934621 55.59660446405797, 12.858390025890811 55.596879690451296, 12.858554538072346 55.597246658975735, 12.85871905025388 55.59706317471351, 12.858554538072346 55.59697143258241, 12.859212586798483 55.59733840110684, 12.859377098980017 55.59752188536906, 12.859377098980017 55.597613627500166, 12.860528684250756 55.59807233815572, 12.860199659887687 55.59807233815572, 12.860035147706155 55.597980596024605, 12.85987063552462 55.597888853893494, 12.859048074616949 55.59779711176239, 12.859212586798483 55.597888853893494, 12.858883562435414 55.59862279094237, 12.858883562435414 55.598989759466804, 12.861022220795359 55.60009066504011, 12.860857708613825 55.60036589143344, 12.861515757339962 55.60064111782677, 12.861022220795359 55.60082460208899, 12.86069319643229 55.60073285995788, 12.860528684250756 55.600916344220096, 12.860857708613825 55.60109982848232, 12.861680269521496 55.601375054875646, 12.862173806066098 55.60146679700676, 12.863160879155304 55.60201724979341, 12.86381892788144 55.60183376553119, 12.864476976607577 55.60183376553119, 12.865135025333714 55.60210899192452, 12.865135025333714 55.6019255076623, 12.865299537515249 55.60146679700676, 12.86497051315218 55.601375054875646, 12.86497051315218 55.60109982848232, 12.866286610604453 55.60155853913786, 12.866122098422919 55.601742023400085, 12.867273683693659 55.60210899192452, 12.86924782987207 55.60339338176005, 12.86974136641667 55.60385209241559, 12.871057463868945 55.60440254520225, 12.87401868313656 55.60632912995554, 12.874676731862698 55.60623738782443, 12.875005756225764 55.60632912995554, 12.875005756225764 55.606512614217756, 12.875170268407299 55.606787840611084, 12.875499292770368 55.60669609847998, 12.877637951130312 55.607980488315505, 12.878460512037984 55.60871442536438, 12.879447585127188 55.609081393888815, 12.879447585127188 55.60926487815104, 12.879776609490257 55.60926487815104, 12.879776609490257 55.609540104544365, 12.880105633853326 55.609448362413254, 12.880434658216393 55.60972358880658, 12.880928194760996 55.60981533093769, 12.881586243487133 55.610274041593236, 12.88109270694253 55.61036578372435, 12.881421731305599 55.61045752585545, 12.881915267850202 55.61045752585545, 12.882408804394803 55.61073275224878, 12.882573316576337 55.61119146290433, 12.88306685312094 55.611283205035434, 12.88306685312094 55.61155843142876, 12.885205511480885 55.61256759487097, 12.88734416984083 55.61403546896871, 12.887837706385431 55.614310695362036, 12.888002218566966 55.614677663886475, 12.8881667307485 55.615044632410914, 12.888495755111569 55.61531985880424, 12.888495755111569 55.61550334306646, 12.888495755111569 55.61568682732868, 12.888824779474637 55.61568682732868, 12.888660267293103 55.61596205372201, 12.889153803837706 55.61632902224644, 12.889153803837706 55.61660424863977, 12.88931831601924 55.61678773290199, 12.889647340382307 55.61715470142642, 12.889482828200775 55.617338185688645, 12.89014087692691 55.61761341208197, 12.889976364745376 55.6178886384753, 12.889976364745376 55.61816386486863, 12.890469901289979 55.618439091261955, 12.890305389108445 55.61862257552417, 12.890469901289979 55.61880605978639, 12.89014087692691 55.619081286179714, 12.89014087692691 55.61926477044194, 12.89129246219765 55.62009044962192, 12.891456974379183 55.62036567601525, 12.891456974379183 55.62073264453968, 12.891950510923786 55.62100787093301, 12.891785998742252 55.62119135519523, 12.89211502310532 55.62146658158856, 12.891950510923786 55.62165006585077, 12.89244404746839 55.621833550112996, 12.892279535286855 55.62201703437521, 12.892608559649924 55.62229226076854, 12.892773071831458 55.622475745030755, 12.892937584012993 55.62275097142408, 12.893102096194527 55.62302619781741, 12.892937584012993 55.62320968207963, 12.893266608376061 55.62348490847296, 12.893431120557594 55.62376013486629, 12.893760144920662 55.62421884552183, 12.894089169283731 55.62449407191516, 12.893924657102197 55.624677556177375, 12.895569778917539 55.625870203881796, 12.89639233982521 55.62614543027512, 12.897543925095949 55.62651239879956, 12.89836648600362 55.62669588306178, 12.899024534729756 55.62678762519289, 12.899682583455894 55.626879367323994, 12.90149221745277 55.627338077979545, 12.904782461083453 55.628072015028415, 12.905275997628056 55.62816375715953, 12.906098558535728 55.628438983552854, 12.90774368035107 55.62898943633951, 12.909553314347946 55.629631631257276, 12.910211363074081 55.6299068576506, 12.911362948344822 55.630273826175035, 12.912185509252492 55.630640794699474, 12.913172582341698 55.63082427896169, 12.914324167612437 55.63109950535502, 12.916462825972381 55.63210866879722, 12.91876599651386 55.634126995681626, 12.918601484332326 55.63431047994384, 12.919095020876927 55.63449396420606, 12.919917581784599 55.63550312764826, 12.920246606147668 55.63605358043492, 12.920904654873805 55.63669577535268, 12.92156270359994 55.63724622813933, 12.92189172796301 55.6378884230571, 12.922878801052214 55.639539781417064, 12.922878801052214 55.63981500781039, 12.923207825415282 55.63981500781039, 12.92386587414142 55.640365460597046, 12.92468843504909 55.64155810830147, 12.924359410686023 55.641925076825906, 12.924523922867557 55.642200303219234, 12.924359410686023 55.64238378748145, 12.92468843504909 55.64265901387478, 12.924359410686023 55.642934240268104, 12.92468843504909 55.643025982399216, 12.924523922867557 55.64348469305476, 12.92468843504909 55.643668177316975, 12.924523922867557 55.6439434037103, 12.924194898504489 55.64495256715251, 12.924359410686023 55.645227793545835, 12.92468843504909 55.645227793545835, 12.9329140441258 55.650365352887945, 12.926991605590569 55.65531942796785, 12.925510995956762 55.65660381780337, 12.925181971593693 55.6568790441967, 12.924852947230624 55.65715427059003, 12.924852947230624 55.65733775485224, 12.925017459412159 55.657521239114466, 12.924852947230624 55.65770472337668, 12.925181971593693 55.65797994977001, 12.924852947230624 55.65825517616334, 12.924852947230624 55.65843866042555, 12.925181971593693 55.658530402556664, 12.924852947230624 55.6588973710811, 12.925017459412159 55.65917259747443, 12.92468843504909 55.65953956599886, 12.925181971593693 55.659723050261086, 12.92468843504909 55.66018176091663, 12.925017459412159 55.66054872944107, 12.924523922867557 55.66100744009661, 12.924523922867557 55.661190924358834, 12.924852947230624 55.66128266648994, 12.924359410686023 55.661833119276594, 12.924523922867557 55.66210834566992, 12.924030386322954 55.662658798456576, 12.92468843504909 55.6629340248499, 12.925017459412159 55.6629340248499, 12.925675508138296 55.663025766981015, 12.926169044682899 55.663117509112126, 12.9266625812275 55.663117509112126, 12.926004532501365 55.663851446161, 12.925346483775227 55.66458538320987, 12.925181971593693 55.66458538320987, 12.925181971593693 55.66495235173431, 12.926333556864433 55.665319320258746, 12.926827093409035 55.66550280452096, 12.927649654316706 55.66559454665207, 12.92814319086131 55.66614499943873, 12.92896575176898 55.66623674156983, 12.929623800495117 55.66651196796316, 12.930117337039718 55.66660371009427, 12.930610873584321 55.66678719435649, 12.933078556307334 55.667521131405366, 12.933407580670403 55.667429389274254, 12.933078556307334 55.66770461566758, 12.933901117215004 55.66807158419202, 12.934394653759608 55.66797984206091, 12.936204287756484 55.668530294847564, 12.936862336482621 55.66880552124089, 12.937355873027224 55.668897263372, 12.939000994842566 55.669355974027546, 12.938671970479497 55.66963120042087, 12.9391655070241 55.6699064268142, 12.988848185847425 55.631925184535, 12.988848185847425 55.6699064268142, 12.719870769039025 55.6699064268142, 12.719870769039025 55.516605325730765)))";
        Geometry geometry = new WKTReader().read(wkt);
        Geometry transformed = PolygonTransformer.transformInvalidPolygon(geometry);
        assertTrue("isValid", transformed.isValid());

    }

}